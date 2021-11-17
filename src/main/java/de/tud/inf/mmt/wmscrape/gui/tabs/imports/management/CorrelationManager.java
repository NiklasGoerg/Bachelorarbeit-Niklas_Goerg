package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.CorrelationType;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
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
    private StockDataColumnRepository stockDataColumnRepository;


    private ObservableList<ExcelCorrelation> stockColumnRelations = FXCollections.observableArrayList();
    private ObservableList<ExcelCorrelation> transactionColumnRelations = FXCollections.observableArrayList();
    
    private HashMap<String, ColumnDatatype> transactionColumnsWithType;

    public ObservableList<ExcelCorrelation> getStockColumnRelations() {
        return stockColumnRelations;
    }

    public ObservableList<ExcelCorrelation> getTransactionColumnRelations() {
        return transactionColumnRelations;
    }

    public HashMap<String, ColumnDatatype> getTransactionColumnsWithType() {
        return transactionColumnsWithType;
    }

    @PostConstruct
    private void init() {
        // this is fixed because the fields of the transaction object are too
        transactionColumnsWithType =  new HashMap<>();
        transactionColumnsWithType.put("depot_name", ColumnDatatype.TEXT);
        transactionColumnsWithType.put("wertpapier_isin", ColumnDatatype.TEXT);
        transactionColumnsWithType.put("transaktions_datum", ColumnDatatype.DATE);
        transactionColumnsWithType.put("transaktionstyp", ColumnDatatype.TEXT);
        transactionColumnsWithType.put("anzahl", ColumnDatatype.INT);
        transactionColumnsWithType.put("währung", ColumnDatatype.TEXT);
        transactionColumnsWithType.put("preis", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("wert_in_eur", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("bankprovision", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("maklercourtage", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("börsenplatzgebühr", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("spesen", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("kapitalertragssteuer", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("solidaritätssteuer", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("quellensteuer", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("abgeltungssteuer", ColumnDatatype.DOUBLE);
        transactionColumnsWithType.put("kirchensteuer", ColumnDatatype.DOUBLE);
    }

    public void fillStockDataCorrelationTable(TableView<ExcelCorrelation> stockDataCorrelationTable, ExcelSheet excelSheet) {

        // add comboboxes...
        prepareCorrelationTable(stockDataCorrelationTable);

        stockColumnRelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockDbCols = new ArrayList<>();

        // using excelSheet.getExcelCorrelations() accesses the excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        // therefore i have to fetch them manually
        for (ExcelCorrelation excelCorrelation : getAllByExcelSheetId(excelSheet)) {
            if (excelCorrelation.getCorrelationType() == CorrelationType.STOCKDATA) {
                stockColumnRelations.add(excelCorrelation);
                addedStockDbCols.add(excelCorrelation.getDbColTitle());
            }
        }

        // add correlation for missing stock db columns
        // only excel col title+number left to set
        for (StockDataDbTableColumn stockColumn : stockDataColumnRepository.findAll()) {
            //datum is set automatically
            String name = stockColumn.getName();

            if (!name.equals("datum") && !addedStockDbCols.contains(name)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation();
                excelCorrelation.setCorrelationType(CorrelationType.STOCKDATA);
                excelCorrelation.setExcelSheet(excelSheet);
                excelCorrelation.setStockDataTableColumn(stockColumn);
                excelCorrelation.setDbColTitle(stockColumn.getName());

                addedStockDbCols.add(name);
                stockColumnRelations.add(excelCorrelation);
            }
        }

        //stockDataCorrelationTable.setMinHeight(stockColumnRelations.size() * 32.5);

        stockDataCorrelationTable.getItems().addAll(stockColumnRelations);
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

        // to be able to undo selection
        comboBoxOptions.add(0, null);


        TableColumn<ExcelCorrelation, String> stockDbColumn = new TableColumn<>("Datenbank Spalten");
        TableColumn<ExcelCorrelation, String> excelColumn = new TableColumn<>("Excel Spalten");

        //stockDbColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        //excelColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        // populate with name from ExcelCorrelation property
        stockDbColumn.setCellValueFactory(new PropertyValueFactory<>("dbColTitle"));



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

        table.getColumns().add(stockDbColumn);
        table.getColumns().add(excelColumn);
    }

    public void fillTransactionCorrelationTable(TableView<ExcelCorrelation> transactionCorrelationTable, ExcelSheet excelSheet) {
        prepareCorrelationTable(transactionCorrelationTable);


        transactionColumnRelations = FXCollections.observableArrayList();
        ArrayList<String> addedTransDbCols = new ArrayList<>();

        // using excelSheet.getExcelCorrelations() accesses the excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        // therefore i have to fetch them manually
        for (ExcelCorrelation excelCorrelation : getAllByExcelSheetId(excelSheet)) {
            if (excelCorrelation.getCorrelationType() == CorrelationType.TRANSACTION) {
                transactionColumnRelations.add(excelCorrelation);
                addedTransDbCols.add(excelCorrelation.getDbColTitle());
            }
        }


        for (String colName : transactionColumnsWithType.keySet()) {
            if (!addedTransDbCols.contains(colName)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation();
                excelCorrelation.setCorrelationType(CorrelationType.TRANSACTION);
                excelCorrelation.setExcelSheet(excelSheet);
                excelCorrelation.setDbColTitle(colName);
                addedTransDbCols.add(colName);
                transactionColumnRelations.add(excelCorrelation);
            }
        }

        transactionCorrelationTable.getItems().addAll(transactionColumnRelations);
    }

    private List<ExcelCorrelation> getAllByExcelSheetId(ExcelSheet excelSheet) {
        return excelCorrelationRepository.findAllByExcelSheetId(excelSheet.getId());
    }
}