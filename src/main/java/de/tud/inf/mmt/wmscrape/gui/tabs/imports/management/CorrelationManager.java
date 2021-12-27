package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.CorrelationType;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Lazy
public class CorrelationManager {
    @Autowired
    private ParsingManager parsingManager;
    @Autowired
    private ExcelCorrelationRepository excelCorrelationRepository;
    @Autowired
    private StockColumnRepository stockColumnRepository;
    @Autowired
    private TransactionColumnRepository transactionColumnRepository;
    @Autowired
    private ImportTabManager importTabManager;

    private ObservableList<ExcelCorrelation> stockColumnRelations = FXCollections.observableArrayList();
    private ObservableList<ExcelCorrelation> transactionColumnRelations = FXCollections.observableArrayList();

    public ObservableList<ExcelCorrelation> getStockColumnRelations() {
        return stockColumnRelations;
    }

    public ObservableList<ExcelCorrelation> getTransactionColumnRelations() {
        return transactionColumnRelations;
    }

    private static final Map<String, ColumnDatatype> importantStockCorrelations = new LinkedHashMap<>();
    private static final Map<String, ColumnDatatype> importantTransactionCorrelations = new LinkedHashMap<>();

    public CorrelationManager() {
        importantTransactionCorrelations.put("depot_name", ColumnDatatype.TEXT);
        importantTransactionCorrelations.put("wertpapier_isin", ColumnDatatype.TEXT);
        importantTransactionCorrelations.put("transaktionstyp", ColumnDatatype.TEXT);
        importantTransactionCorrelations.put("transaktions_datum", ColumnDatatype.TEXT);

        importantStockCorrelations.put("isin", ColumnDatatype.TEXT);
        importantStockCorrelations.put("wkn", ColumnDatatype.TEXT);
        importantStockCorrelations.put("name", ColumnDatatype.TEXT);
        importantStockCorrelations.put("typ", ColumnDatatype.TEXT);
    }


    public boolean fillCorrelationTables(TableView<ExcelCorrelation> stockDataCorrelationTable,
                                      TableView<ExcelCorrelation> transactionCorrelationTable, ExcelSheet excelSheet) {
        List<ExcelCorrelation> correlations = excelCorrelationRepository.findAllByExcelSheetId(excelSheet.getId());
        boolean allValid = validateCorrelations(correlations);
        fillStockDataCorrelationTable(stockDataCorrelationTable, excelSheet, correlations);
        fillTransactionCorrelationTable(transactionCorrelationTable,excelSheet, correlations);
        return allValid;
    }

    /**
     * validate that the correlations still match
     */
    public boolean validateCorrelations(List<ExcelCorrelation> correlations) {
        Map<String, Integer> titleIndexMap = parsingManager.getTitleToExcelIndex();
        boolean allValid = true;

        for(var correlation : correlations) {
            String excelColTitle = correlation.getExcelColTitle();
            Integer index = titleIndexMap.getOrDefault(excelColTitle,null);
            // there was/is a correlation && (not found in sheet || index not matching)
            if(excelColTitle != null && (index == null || correlation.getExcelColNumber() != index)) {
                // reset correlation
                importTabManager.addToLog("ERR:\t\tZurückgesetzte DB-Spalte: '"+correlation.getDbColTitle()+
                                                "'. Vorher zugeordnet: '"+correlation.getExcelColTitle()+"'");
                correlation.setExcelColNumber(-1);
                correlation.setExcelColTitle(null);
                allValid = false;
            }
        }
        return allValid;
    }

    @Transactional
    public void fillStockDataCorrelationTable(TableView<ExcelCorrelation> stockDataCorrelationTable, ExcelSheet excelSheet,
                                              List<ExcelCorrelation> correlations) {

        // add comboboxes
        prepareCorrelationTable(stockDataCorrelationTable);

        stockColumnRelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockDbCols = new ArrayList<>();

        // using excelSheet.getExcelCorrelations() accesses the excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        for (ExcelCorrelation excelCorrelation : correlations) {
            if (excelCorrelation.getCorrelationType() == CorrelationType.STOCKDATA) {
                stockColumnRelations.add(excelCorrelation);
                addedStockDbCols.add(excelCorrelation.getDbColTitle());
            }
        }

        // even if they are given in the db i want them on top
        addImportantCorrelations(addedStockDbCols, excelSheet, importantStockCorrelations, CorrelationType.STOCKDATA, stockColumnRelations);


        // add correlation for missing stock db columns
        for (StockColumn stockColumn : stockColumnRepository.findAll()) {
            //datum is set automatically
            String name = stockColumn.getName();

            if (!name.equals("datum") && !addedStockDbCols.contains(name)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation(CorrelationType.STOCKDATA, excelSheet, stockColumn);
                addedStockDbCols.add(name);
                stockColumnRelations.add(excelCorrelation);
            }
        }
        stockDataCorrelationTable.getItems().addAll(stockColumnRelations);
    }

    private void addImportantCorrelations(List<String> added, ExcelSheet sheet, Map<String, ColumnDatatype> cols,
                                          CorrelationType type, ObservableList<ExcelCorrelation> list) {

        for(var entry : cols.entrySet()) {
            if (!added.contains(entry.getKey())) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation(type, sheet, entry.getValue(), entry.getKey());
                added.add(excelCorrelation.getDbColTitle());
                list.add(excelCorrelation);
            }
        }
    }

    private Integer getExcelColNumber(String newValue) {
        return parsingManager.getTitleToExcelIndex().getOrDefault(newValue, -1);
    }

    private ObservableList<String> mapToObservableList(Map<Integer, String> map) {
        ObservableList<String> excelColTitles = FXCollections.observableArrayList();
        excelColTitles.addAll(map.values());
        return excelColTitles;
    }

    private void prepareCorrelationTable(TableView<ExcelCorrelation> table) {
        // could be done better
        // normal program structure guarantees that this is accessed after table load
        ObservableList<String> comboBoxOptions = mapToObservableList(parsingManager.getIndexToExcelTitle());

        // to be able to undo a selection
        comboBoxOptions.add(0, null);

        TableColumn<ExcelCorrelation, String> dbColumn = new TableColumn<>("Datenbank Spalte");
        TableColumn<ExcelCorrelation, String> typeColumn = new TableColumn<>("DB-Typ");
        TableColumn<ExcelCorrelation, String> excelColumn = new TableColumn<>("Excel Spalte");

        // populate with name from ExcelCorrelation property
        dbColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDbColTitle()));
        typeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDbColDataType().name()));

        // choiceBox
        excelColumn.setCellValueFactory(param -> param.getValue().excelColTitleProperty());
        excelColumn.setCellFactory(param -> {
            ComboBoxTableCell<ExcelCorrelation, String> cell = new ComboBoxTableCell<>();
            cell.getItems().addAll(comboBoxOptions);

            // auto update number from title on change
            cell.itemProperty().addListener((o, ov, nv) -> {
                if (excelColumn.getTableView() != null && excelColumn.getTableView().getSelectionModel().getSelectedItem() != null) {
                    ExcelCorrelation correlation = excelColumn.getTableView().getSelectionModel().getSelectedItem();
                    correlation.setExcelColNumber(getExcelColNumber(correlation.getExcelColTitle()));
                }
            });
            return cell;
        });

        table.getColumns().add(dbColumn);
        table.getColumns().add(typeColumn);
        table.getColumns().add(excelColumn);
    }

    public void fillTransactionCorrelationTable(TableView<ExcelCorrelation> transactionCorrelationTable,
                                                ExcelSheet excelSheet, List<ExcelCorrelation> correlations) {
        prepareCorrelationTable(transactionCorrelationTable);

        transactionColumnRelations = FXCollections.observableArrayList();
        ArrayList<String> addedTransDbCols = new ArrayList<>();
        // dont need the key of depot
        addedTransDbCols.add("depot_id");

        // using excelSheet.getExcelCorrelations() accesses the excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        // therefore I have to fetch them manually
        for (ExcelCorrelation excelCorrelation : correlations) {
            if (excelCorrelation.getCorrelationType() == CorrelationType.TRANSACTION) {
                transactionColumnRelations.add(excelCorrelation);
                addedTransDbCols.add(excelCorrelation.getDbColTitle());
            }
        }

        // even if they are given in the db i want them on top
        addImportantCorrelations(addedTransDbCols, excelSheet, importantTransactionCorrelations, CorrelationType.TRANSACTION, transactionColumnRelations);


        for (TransactionColumn column : transactionColumnRepository.findAll()) {
            String name = column.getName();

            if (!addedTransDbCols.contains(name)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation(CorrelationType.TRANSACTION, excelSheet, column);
                addedTransDbCols.add(name);
                transactionColumnRelations.add(excelCorrelation);
            }
        }

        transactionCorrelationTable.getItems().addAll(transactionColumnRelations);
    }
}