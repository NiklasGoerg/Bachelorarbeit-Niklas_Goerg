package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Lazy
public class ExtractionManager {
    // POI: index 0, EXCEL Index: 1y
    private final static int OFFSET = 1;
    @Autowired
    private ImportTabManager importTabManager;
    @Autowired
    private DbTransactionManager dbTransactionManager;
    @Autowired
    private StockDataDbManager stockDataDbManager;
    @Autowired
    private ParsingManager parsingManager;
    @Autowired
    private CorrelationManager correlationManager;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private DepotRepository depotRepository;

    public int startDataExtraction() {
        
        if (!isInExtractableState()) return -2;
        if (!correlationsHaveValidState()) return -3;


        int stockExtractionResult = extractStockData();

        // 0-OK, -1-SilentError -> below other error
        // don't break execution if silent
        // only silent and ok can pass
        if (stockExtractionResult < -1) {
            return stockExtractionResult;
        }

        // create stocks if not existing based on prior imported stock data
        stockDataDbManager.createMissingStocks();

        int transactionExtractionResult = extractTransactionData();

        // 0-OK, -1-SilentError -> below other error
        // only ok can pass
        if (transactionExtractionResult != 0) {
            return transactionExtractionResult;
        }

        // transaction ok but stock had a silent error
        if (stockExtractionResult == -1) return -1;
        return 0;
    }

    private int extractStockData() {

        importTabManager.addToLog("##### Start Stammdaten-Import #####\n");

        // execution is not stopped at a silent error but a log message is added
        boolean silentError = false;
        Connection connection = stockDataDbManager.getConnection();
        HashMap<String, PreparedStatement> statements = dbTransactionManager.createStockDataStatements(connection);
        HashMap<String, ColumnDatatype> columnDatatypes = dbTransactionManager.getStockColDbDatatypes();

        var excelSheetRows = parsingManager.getExcelSheetRows();
        var stockColumnRelations = correlationManager.getStockColumnRelations();


        if (statements == null) return -4;
        Date dateToday = new Date(System.currentTimeMillis());

        // go through all rows
        for (int row : excelSheetRows.keySet()) {

            // skip rows if not selected
            if (!(parsingManager.getSelectedStockDataRows().get(row).get())) {
                //addToLog("INFO: Stammdaten von Zeile: " + (row+OFFSET) + " ist nicht markiert.");
                continue;
            }

            // the columns for one row
            ArrayList<String> rowData = excelSheetRows.get(row);

            int isinCol = getColNrByName("isin", stockColumnRelations);

            // check if isin valid
            String isin = rowData.get(isinCol);
            if (isin == null || isin.isBlank() || isin.length() >= 50) {
                silentError = true;
                importTabManager.addToLog("FEHLER: Isin der Zeile " + (row+OFFSET) + " leer oder länger als 50 Zeichen. ->'" + isin + "'");
                continue;
            }

            int wknCol = getColNrByName("wkn", stockColumnRelations);

            // check if wkn valid
            String wkn = rowData.get(wknCol);
            if (wkn == null || wkn.isBlank()) {
                silentError = true;
                importTabManager.addToLog("FEHLER: Wkn der Zeile " + (row+OFFSET) + " leer. ->'" + wkn + "'");
                continue;
            }

            // pick one column per relation from row
            for (ExcelCorrelation correlation : stockColumnRelations) {
                String dbColName = correlation.getDbColTitle();

                // continue bcs the key value is not inserted with a prepared statement
                if (dbColName.equals("isin")) continue;

                int correlationColNumber = correlation.getExcelColNumber();
                String colData;

                // -1 is default and can't be set another way meaning it's not set
                if (correlationColNumber == -1) {
                    //addToLog("INFO: Die Spalte '" + dbColName +"' hat keine Zuordnung.");
                    colData = null;
                } else {
                    colData = rowData.get(correlationColNumber);
                    if (colData.isBlank()) colData = null;
                }


                ColumnDatatype datatype = columnDatatypes.getOrDefault(dbColName, null);
                if (datatype == null) {
                    silentError = true;
                    importTabManager.addToLog("Fehler: Der Datenbankspalte " + dbColName
                            + " ist kein Datentyp zugeordnet.");
                    continue;
                }

                if (!matchingDataType(datatype, colData)) {
                    silentError = true;
                    importTabManager.addToLog("Fehler: Der Datentyp der Zeile " + (row+OFFSET) + " in der Spalte '" + correlation.getExcelColTitle() +
                            "', stimmt nicht mit dem der Datenbankspalte " + dbColName + " vom Typ " + datatype.name() +
                            " überein. Zellendaten: '" + colData + "'");
                    continue;
                }

                // statement exists bcs if there is an error at statement creation
                // the program does not reach this line
                PreparedStatement statement = statements.getOrDefault(dbColName, null);

                if (statement == null) {
                    silentError = true;
                    importTabManager.addToLog("Fehler: Sql-Statment für die Spalte '" + correlation.getExcelColTitle() +
                            "' nicht gefunden");
                    continue;
                }

                silentError &= dbTransactionManager.fillStockStatementAddToBatch(isin, dateToday, statement, colData, datatype);
            }

        }

        dbTransactionManager.executeStatements(connection, statements);
        importTabManager.addToLog("\n##### Ende Stammdaten-Import #####\n");

        if (silentError) return -1;
        return 0;
    }

    private int extractTransactionData() {
        importTabManager.addToLog("##### Start Transaktions Import #####\n");

        // execution is not stopped at a silent error but a log message is added
        boolean silentError = false;
        Connection connection = stockDataDbManager.getConnection();
        HashMap<String, PreparedStatement> statements = dbTransactionManager.createTransactionDataStatements(connection);
        var excelSheetRows = parsingManager.getExcelSheetRows();
        var transactionColumnRelations = correlationManager.getTransactionColumnRelations();


        if (statements == null) return -4;

        // transactionColumnNames
        int isinCol = getColNrByName("wertpapier_isin", transactionColumnRelations);
        int dateCol = getColNrByName("transaktions_datum", transactionColumnRelations);
        int depotNameCol = getColNrByName("depot_name", transactionColumnRelations);

        // go through all rows
        for (int row : excelSheetRows.keySet()) {
            // skip rows if not selected
            if (!parsingManager.getSelectedTransactionRows().get(row).get()) continue;

            // the columns for one row
            ArrayList<String> rowData = excelSheetRows.get(row);

            String depotName = rowData.get(depotNameCol);
            if (depotName == null || depotName.isBlank() || depotName.length() >= 50) {
                importTabManager.addToLog("FEHLER: Depotname der Zeile " + (row+OFFSET) + " fehlerhaft oder leer. Wert: '"
                        + depotName + "' ");
                silentError = true;
                continue;
            }

            String isin = rowData.get(isinCol);
            if (isin == null || isin.isBlank() || isin.length() >= 50) {
                importTabManager.addToLog("FEHLER: Isin der Zeile " + (row+OFFSET) + " fehlerhaft, leer oder länger als 50 Zeichen. Wert: '" + isin + "'");
                silentError = true;
                continue;
            }

            String date = rowData.get(dateCol);
            if (!matchingDataType(ColumnDatatype.DATE, date)) {
                importTabManager.addToLog("FEHLER: Transaktionsdatum '" + date + "' der Zeile " + (row+OFFSET) + " ist fehlerhaft.");
                silentError = true;
                continue;
            }

            Date parsedDate = Date.valueOf(date);

            // stocks are created beforehand
            var stock = stockRepository.findByIsin(isin);
            if (stock.isEmpty()) {
                importTabManager.addToLog("FEHLER: Das passende Wertpapier zur Transaktion aus Zeile " + (row+OFFSET) +
                        " konnte nicht gefunden werden. Angegebene Isin: '" + isin + "'");
                silentError = true;
                continue;
            }

            // search if the depot already exists or creates a new one
            Depot depot = depotRepository.findByName(depotName).orElse(null);

            if (depot == null) {
                importTabManager.addToLog("INFO: Erstelle Depot mit dem Namen: " + depotName);
                depot = new Depot(depotName);
                depotRepository.save(depot);
            }

            // using autogenerated id after save
            int depotId = depot.getId();


            for (ExcelCorrelation correlation : transactionColumnRelations) {
                String dbColName = correlation.getDbColTitle();

                // already set before don't do it again
                if (dbColName.equals("depot_name") || dbColName.equals("transaktions_datum") || dbColName.equals("wertpapier_isin")) {
                    continue;
                }

                int correlationColNumber = correlation.getExcelColNumber();
                String colData;

                // -1 is default and can't be set another way meaning it's not set
                if (correlationColNumber == -1) {
                    //addToLog("INFO: Die Spalte '" + dbColName +"' hat keine Zuordnung.");
                    colData = null;
                } else {
                    colData = rowData.get(correlationColNumber);
                    // change to null to override possible existing values
                    if (colData.isBlank()) colData = null;
                }

                var transactionColumnsWithType = correlationManager.getTransactionColumnsWithType();
                ColumnDatatype colDatatype = transactionColumnsWithType.getOrDefault(dbColName, ColumnDatatype.INVALID);

                if (!matchingDataType(colDatatype, colData)) {
                    importTabManager.addToLog("FEHLER: Der Wert der Zelle in der Zeile: " + row + " Spalte: '"
                            + correlation.getExcelColTitle() + "' hat nicht den passenden Datentyp für '"
                            + dbColName + "' vom Typ '" + colDatatype + "'. Wert: '" + colData + "'");
                    silentError = true;
                    continue;
                }

                PreparedStatement statement = statements.getOrDefault(dbColName, null);

                if (statement == null) {
                    silentError = true;
                    importTabManager.addToLog("Fehler: Sql-Statment für die Spalte '" + correlation.getExcelColTitle() +
                            "' nicht gefunden");
                    continue;
                }

                silentError &= dbTransactionManager.fillTransactionStatementAddToBatch(depotId, parsedDate, isin, statement, colData, colDatatype);
            }
        }

        dbTransactionManager.executeStatements(connection, statements);

        importTabManager.addToLog("\n##### Ende Transaktions Import #####\n");
        if (silentError) return -1;
        return 0;
    }

    private boolean matchingDataType(ColumnDatatype colDatatype, String colData) {
        if (colDatatype == null) {
            return false;
        } else if (colData == null) {
            // null is valid in order to override values that may be set in the wrong column
            return true;
        } else if (colDatatype == ColumnDatatype.INT && colData.matches("^[\\-+]?[0-9]+(\\.0{5})?$")) {
            // normal format would be "^-?[0-9]+$" but because of
            // String.format("%.5f", cell.getNumericCellValue()).replace(",",".");
            // 5 zeros are added to int
            return true;
        } else if (colDatatype == ColumnDatatype.DOUBLE && colData.matches("^[\\-+]?[0-9]+([.,]?[0-9]+)?$")) {
            return true;
        } else if (colDatatype == ColumnDatatype.DATE && colData.matches("^[1-9][0-9]{3}-[0-9]{2}-[0-9]{2}$")) {
            return true;
        } else return colDatatype == ColumnDatatype.TEXT;
    }

    private int getColNrByName(String name, ObservableList<ExcelCorrelation> correlations) {
        for (ExcelCorrelation correlation : correlations) {
            if (correlation.getDbColTitle().equals(name)) {
                return correlation.getExcelColNumber();
            }
        }
        return -1;
    }

    private boolean isInExtractableState() {
        return parsingManager.getExcelSheetRows() != null && parsingManager.getSelectedTransactionRows() != null && parsingManager.getSelectedStockDataRows() != null &&
                correlationManager.getStockColumnRelations().size() != 0 && correlationManager.getTransactionColumnRelations().size() != 0;
    }

    private boolean correlationsHaveValidState() {
        if (getColNrByName("isin", correlationManager.getStockColumnRelations()) == -1) return false;
        if (getColNrByName("wkn", correlationManager.getStockColumnRelations()) == -1) return false;
        if (getColNrByName("name", correlationManager.getStockColumnRelations()) == -1) return false;
        if (getColNrByName("wertpapier_isin", correlationManager.getTransactionColumnRelations()) == -1) return false;
        if (getColNrByName("transaktions_datum", correlationManager.getTransactionColumnRelations()) == -1) return false;
        if (getColNrByName("transaktionstyp", correlationManager.getTransactionColumnRelations()) == -1) return false;
        return getColNrByName("depot_name", correlationManager.getTransactionColumnRelations()) != -1;
    }
}