package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, VisualDatatype> importantStockCorrelations = new LinkedHashMap<>();
    private static final Map<String, VisualDatatype> importantTransactionCorrelations = new LinkedHashMap<>();

    /**
     * called at bean creation.
     *
     * these elements are guaranteed to be included (also at the top) in the correlation lists when creating the preview.
     * maybe one could move this to {@link de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager}
     * and {@link de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockTableManager}
     */
    public CorrelationManager() {
        importantTransactionCorrelations.put("depot_name", VisualDatatype.Text);
        importantTransactionCorrelations.put("wertpapier_isin", VisualDatatype.Text);
        importantTransactionCorrelations.put("transaktions_datum", VisualDatatype.Datum);

        importantStockCorrelations.put("datum", VisualDatatype.Datum);
        importantStockCorrelations.put("isin", VisualDatatype.Text);
        importantStockCorrelations.put("wkn", VisualDatatype.Text);
        importantStockCorrelations.put("name", VisualDatatype.Text);
        importantStockCorrelations.put("typ", VisualDatatype.Text);
        importantStockCorrelations.put("r_par", VisualDatatype.Int);

    }


    /**
     * adds the object to the two correlation tables
     *
     * @param stockDataCorrelationTable the stock correlation table
     * @param transactionCorrelationTable the transaction correlation table
     * @param excelSheet the Excel configuration
     * @return true if all existing correlations matched with the columns in the Excel sheet
     */
    public boolean fillCorrelationTables(TableView<ExcelCorrelation> stockDataCorrelationTable,
                                      TableView<ExcelCorrelation> transactionCorrelationTable, ExcelSheet excelSheet) {
        List<ExcelCorrelation> correlations = excelCorrelationRepository.findAllByExcelSheetId(excelSheet.getId());
        boolean allValid = validateCorrelations(correlations);

        stockDataCorrelationTable.getColumns().clear();
        stockDataCorrelationTable.getItems().clear();
        transactionCorrelationTable.getColumns().clear();
        transactionCorrelationTable.getItems().clear();

        fillStockDataCorrelationTable(stockDataCorrelationTable, excelSheet, correlations);
        fillTransactionCorrelationTable(transactionCorrelationTable,excelSheet, correlations);
        return allValid;
    }

    /**
     * validate that the correlations still match (right name/title at the correct Excel column index)
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

    /**
     * adds the objects for the stock correlation table
     *
     * @param stockDataCorrelationTable the javafx table
     * @param excelSheet the Excel configuration
     * @param correlations all correlations for the Excel configuration. only those of type "stock" are used
     */
    @Transactional
    public void fillStockDataCorrelationTable(TableView<ExcelCorrelation> stockDataCorrelationTable, ExcelSheet excelSheet,
                                              List<ExcelCorrelation> correlations) {

        // add comboboxes
        prepareCorrelationTable(stockDataCorrelationTable);

        ArrayList<String> addedStockDbCols = new ArrayList<>();

        // using excelSheet.getExcelCorrelations() accesses the Excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        for (ExcelCorrelation excelCorrelation : correlations) {
            if (excelCorrelation.getCorrelationType() == CorrelationType.STOCKDATA) {
                stockDataCorrelationTable.getItems().add(excelCorrelation);
                addedStockDbCols.add(excelCorrelation.getDbColTitle());
            }
        }

        // even if they are given in the db I want them on top
        addImportantCorrelations(stockDataCorrelationTable, addedStockDbCols, excelSheet,
                importantStockCorrelations, CorrelationType.STOCKDATA);


        // add correlation for missing stock db columns
        for (StockColumn stockColumn : stockColumnRepository.findAll()) {
            String name = stockColumn.getName();

            if (!addedStockDbCols.contains(name)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation(CorrelationType.STOCKDATA, excelSheet, stockColumn);
                addedStockDbCols.add(name);
                stockDataCorrelationTable.getItems().add(excelCorrelation);
            }
        }
    }

    /**
     * adds additional correlations for some specific database columns e.g. key columns
     *
     * @param stockDataCorrelationTable the stock javafx correlation table
     * @param added columns already added to the table
     * @param sheet the Excel configuration
     * @param cols the map which holds the datatypes given the column name
     * @param type stock or transaction correlation type
     */
    private void addImportantCorrelations(TableView<ExcelCorrelation> stockDataCorrelationTable, List<String> added, ExcelSheet sheet,
                                          Map<String, VisualDatatype> cols, CorrelationType type) {

        for(var entry : cols.entrySet()) {
            if (!added.contains(entry.getKey())) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation(type, sheet, entry.getValue(), entry.getKey());
                added.add(excelCorrelation.getDbColTitle());
                stockDataCorrelationTable.getItems().add(excelCorrelation);
            }
        }
    }

    /**
     *
     * @param title the name of the column title inside the Excel sheet
     * @return the column index for the title inside the Excel sheet
     */
    private Integer getExcelColNumber(String title) {
        return parsingManager.getTitleToExcelIndex().getOrDefault(title, -1);
    }

    private ObservableList<String> mapToObservableList(Map<Integer, String> map) {
        ObservableList<String> excelColTitles = FXCollections.observableArrayList();
        excelColTitles.addAll(map.values());
        return excelColTitles;
    }

    /**
     * prepares the table so that {@link de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation} can be inserted
     *
     * @param table one of the two correlation tables
     */
    private void prepareCorrelationTable(TableView<ExcelCorrelation> table) {
        // could be done better
        // normal program structure guarantees that this is accessed after table load
        ObservableList<String> comboBoxOptions = mapToObservableList(parsingManager.getIndexToExcelTitle());

        // to be able to undo a selection
        comboBoxOptions.add(0, null);

        TableColumn<ExcelCorrelation, String> dbColumn = new TableColumn<>("Datenbank-Spalte");
        TableColumn<ExcelCorrelation, String> typeColumn = new TableColumn<>("DB-Typ");
        TableColumn<ExcelCorrelation, String> excelColumn = new TableColumn<>("Excel-Spalte");

        // populate with name from ExcelCorrelation property
        dbColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDbColTitle()));
        typeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDbColVisualType().name()));

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

    /**
     * adds the objects for the transaction correlation table
     *
     * @param transactionCorrelationTable the javafx table
     * @param excelSheet the Excel configuration
     * @param correlations all correlations for the Excel configuration. only those of type "stock" are used
     */
    public void fillTransactionCorrelationTable(TableView<ExcelCorrelation> transactionCorrelationTable,
                                                ExcelSheet excelSheet, List<ExcelCorrelation> correlations) {
        prepareCorrelationTable(transactionCorrelationTable);

        ArrayList<String> addedTransDbCols = new ArrayList<>();
        // don't need the key of depot
        addedTransDbCols.add("depot_id");

        // using excelSheet.getExcelCorrelations() accesses the Excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        // therefore I have to fetch them manually
        for (ExcelCorrelation excelCorrelation : correlations) {
            if (excelCorrelation.getCorrelationType() == CorrelationType.TRANSACTION) {
                transactionCorrelationTable.getItems().add(excelCorrelation);
                addedTransDbCols.add(excelCorrelation.getDbColTitle());
            }
        }

        // even if they are given in the db I want them on top
        addImportantCorrelations(transactionCorrelationTable, addedTransDbCols, excelSheet,
                importantTransactionCorrelations, CorrelationType.TRANSACTION);


        for (TransactionColumn column : transactionColumnRepository.findAll()) {
            String name = column.getName();

            if (!addedTransDbCols.contains(name)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation(CorrelationType.TRANSACTION, excelSheet, column);
                addedTransDbCols.add(name);
                transactionCorrelationTable.getItems().add(excelCorrelation);
            }
        }
    }
}