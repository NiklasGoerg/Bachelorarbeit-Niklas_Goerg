package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransactionRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.management.StockDataDbManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional
public class ImportTabManagement {

    @Autowired
    private ExcelSheetRepository excelSheetRepository;
    @Autowired
    private ExcelCorrelationRepository excelCorrelationRepository;
    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    private StockDataDbManager stockDataDbManager;
    @Autowired
    private DepotTransactionRepository depotTransactionRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private DepotRepository depotRepository;


    private static ObservableList<ObservableList<String>> sheetPreviewTableData = FXCollections.observableArrayList();

    private static ObservableMap<Integer, ArrayList<String>> excelSheetRows;

    private static Map<Integer, String> indexToExcelTitle;
    private static Map<String, Integer> titleToExcelIndex;

    // at first, they are equal
    private static Map<Integer, SimpleBooleanProperty> selectedStockDataRows;
    private static Map<Integer, SimpleBooleanProperty> selectedTransactionRows;

    private static ObservableList<ExcelCorrelation> stockColumnRelations = FXCollections.observableArrayList();
    private static ObservableList<ExcelCorrelation> transactionColumnRelations = FXCollections.observableArrayList();

    private SimpleStringProperty logText;

    private static HashMap<String, ColumnDatatype> transactionColumnsWithType;

    @PostConstruct
    private void init() {
        // Integer represents datatype
        // 1-Integer, 2-String, 3-Date, 4-Double
        transactionColumnsWithType =  new HashMap<>();
        transactionColumnsWithType.put("depot_name", ColumnDatatype.TEXT);
        transactionColumnsWithType.put("wertpapier_isin", ColumnDatatype.TEXT);
        transactionColumnsWithType.put("transaktions_datum", ColumnDatatype.DATE);
        transactionColumnsWithType.put("tansaktionstyp", ColumnDatatype.TEXT);
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

//    #########################
//    Controller logic section
//    #########################

    public Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle("-fx-background-color: FF4A4A;");
        return tooltip;
    }

    public void createNewExcel(String description) {
        excelSheetRepository.save(new ExcelSheet(description));
    }

    public void deleteSpecificExcel(ExcelSheet excelSheet) {
        // fix for not working orphan removal
        excelSheet.setExcelCorrelations(new ArrayList<>());
        excelSheetRepository.delete(excelSheet);
    }

    public ObservableList<ExcelSheet> initExcelSheetList(ListView<ExcelSheet> excelSheetList) {
        // https://docs.oracle.com/javafx/2/ui_controls/list-view.htm
        // https://www.baeldung.com/javafx-listview-display-custom-items

        ObservableList<ExcelSheet> excelSheetObservableList = FXCollections.observableList(excelSheetRepository.findAll());
        excelSheetList.setItems(excelSheetObservableList);
        return excelSheetObservableList;
    }

    public List<ExcelSheet> getExcelSheets() {
        return excelSheetRepository.findAll();
    }

    public void saveExcel(ExcelSheet excelSheet) {
        excelSheetRepository.save(excelSheet);

        for(ExcelCorrelation excelCorrelation : stockColumnRelations) {
            excelCorrelationRepository.save(excelCorrelation);
        }

        for(ExcelCorrelation excelCorrelation : transactionColumnRelations) {
            excelCorrelationRepository.save(excelCorrelation);
        }
    }

    public boolean sheetExists(ExcelSheet excelSheet) {
        try {
            File file = new File(excelSheet.getPath());
            return file.exists() && file.isFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void passLogText(SimpleStringProperty logText) {
        this.logText = logText;
    }

    public void addToLog(String line) {
        logText.set(this.logText.getValue() +"\n" + line);
    }

//    #########################
//    Excel parsing section
//    #########################


    public Workbook decryptAndGetWorkbook(ExcelSheet excelSheet) throws EncryptedDocumentException {
        try {
            return WorkbookFactory.create(new File(excelSheet.getPath()), excelSheet.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int fillExcelPreview(TableView<ObservableList<String>> sheetPreviewTable, ExcelSheet excelSheet) throws EncryptedDocumentException{
        sheetPreviewTable.getColumns().clear();
        sheetPreviewTable.getItems().clear();

        Workbook workbook;
        try {
             workbook = decryptAndGetWorkbook(excelSheet);
        } catch (EncryptedDocumentException e) {
            e.printStackTrace();
            // cant decrypt
            return -1;
        }

        if (excelSheet.getTitleRow() > workbook.getSheetAt(0).getLastRowNum() || excelSheet.getTitleRow() <= 0) {
            // column out of bounds
            return -2;
        }

        excelSheetRows = FXCollections.observableMap(new TreeMap<>());
        boolean evalFaults = getExcelSheetData(workbook, excelSheet.getTitleRow(), excelSheetRows);

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(excelSheetRows.size() == 0) {
            return -3;
        }

        removeEmptyRows(excelSheetRows);
        unifyRows(excelSheetRows);
        removeEmptyCols(excelSheetRows, excelSheet);
        indexToExcelTitle = extractColTitles(excelSheet.getTitleRow()-1, excelSheetRows);

        createNormalizedTitles(indexToExcelTitle);
        if(!titlesAreUnique(indexToExcelTitle)) {
            return -4;
        }

        titleToExcelIndex = reverseMap(indexToExcelTitle);

        int selectionColNumber = getSelectionColNumber(indexToExcelTitle, excelSheet);
        if(selectionColNumber == -1) {
            // selection col not found
            return -5;
        }

        selectedStockDataRows = getSelectedInitially(excelSheetRows, selectionColNumber);
        // have to initialize twice because otherwise the same booleanproperties are used
        selectedTransactionRows = getSelectedInitially(excelSheetRows, selectionColNumber);

        addColumnsToView(sheetPreviewTable, indexToExcelTitle, excelSheet);

        // add rows to data observer
        excelSheetRows.forEach((row, rowContent) -> {
            ObservableList<String> tableRow = FXCollections.observableArrayList();
            tableRow.addAll(rowContent);
            sheetPreviewTableData.add(tableRow);
        });

        // add rows themselves
        sheetPreviewTable.setItems(sheetPreviewTableData);
        if(evalFaults) {
            return -6;
        }
        return 0;
    }

    private boolean getExcelSheetData(Workbook workbook, int startRow, ObservableMap<Integer, ArrayList<String>> excelData ) {

        addToLog("##### Start Excel Parsing #####\n");

        Sheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true);
        //DataFormatter formatter = new DataFormatter();
        String value;
        boolean evalFault = false;

        // for each table row
        // excel starts with index 1 "poi" with 0
        for (int rowNumber = startRow-1; rowNumber < sheet.getLastRowNum(); rowNumber++) {

            Row row = sheet.getRow(rowNumber);

            // skip if null
            if (row == null) continue;

            // for each column per row
            for (int colNumber = 0; colNumber < row.getLastCellNum(); colNumber++) {
                Cell cell = row.getCell(colNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                // add new array if new row
                if (!excelData.containsKey(rowNumber)) {
                    excelData.put(rowNumber, new ArrayList<String>());
                    // add row index
                    excelData.get(rowNumber).add(String.valueOf(rowNumber));
                }

                // cell value processing
                if (cell == null) {
                    excelData.get(rowNumber).add("");
                } else {
                    try {
                        switch (evaluator.evaluateInCell(cell).getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                //String raw = formatter.formatCellValue(cell);
                                //if(!raw.matches("^[0-9]+((\\.|,)[0-9]*)?$")) {
                                //    value = raw;
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    java.util.Date date =  new java.util.Date(cell.getDateCellValue().getTime());
                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    value = dateFormat.format(date);
                                } else {
                                    value = String.format("%.5f", cell.getNumericCellValue()).replace(",",".");
                                }
                                break;
                            case BOOLEAN:
                                value = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case ERROR:
                                value = String.valueOf(cell.getErrorCellValue());
                                break;
                            default:
                                value = "";
                                break;
                        }
                    } catch (Exception e) {
                        // TODO: http://poi.apache.org/components/spreadsheet/user-defined-functions.html
                        //      https://poi.apache.org/components/spreadsheet/eval-devguide.html (bottom)
                        // example org.apache.poi.ss.formula.eval.NotImplementedException: Error evaluating cell 'WP Depot'!AX75
                        System.out.println(e.getMessage());
                        String[] cellError = e.toString().split("'");
                        addToLog(e.getMessage() + " _CAUSE:_ " + e.getCause());
                        value = "ERROR: Evaluationsfehler: " + cellError[cellError.length-1];
                        evalFault = true;
                    }
                    excelData.get(rowNumber).add(value);
                }
            }
        }

        addToLog("##### Ende  Excel Parsing #####\n");
        return evalFault;
    }

    private void removeEmptyRows(Map<Integer, ArrayList<String>> excelData) {
        // Remove rows which are empty
        List<Integer> rowsToRemove = new ArrayList<>();

        boolean hasContent;

        for(int key : excelData.keySet()) {
            hasContent = false;
            ArrayList<String> row = excelData.get(key);

            // skip the first col as it is the row index
            for(int col=1; col < row.size(); col++) {
                String cellValue =  row.get(col);

                if (cellValue != null && !cellValue.isBlank()) {
                    hasContent = true;
                    break;
                }
            }

            if (!hasContent) {
                rowsToRemove.add(key);
            }
        }

        rowsToRemove.forEach(excelData::remove);
    }

    private void removeEmptyCols(Map<Integer, ArrayList<String>> rowMap, ExcelSheet excelSheet) {
        // Lists have to be unified beforehand
        List<Integer> colsToRemove = new ArrayList<>();

        boolean hasContent;

        for(int col=1; col < rowMap.get(excelSheet.getTitleRow()-1).size(); col++) {
            hasContent = false;

            for(ArrayList<String> row: rowMap.values()) {
                if(!row.get(col).isBlank()) {
                    hasContent = true;
                    break;
                }
            }
            if (!hasContent) {
                colsToRemove.add(col);
            }
        }

        int counterShrinkage;
        for(int row : rowMap.keySet()) {
            counterShrinkage = 0;
            for(int col : colsToRemove) {
                ArrayList<String> rowData = rowMap.get(row);
                rowData.remove(col-counterShrinkage);
                rowMap.put(row, rowData);
                counterShrinkage++;
            }
        }
    }

    private void unifyRows(Map<Integer,ArrayList<String>> rowMap) {
        // Adds columns to create uniform rows of the same length
        int maxCols = 0;
        int cols;

        for(Integer row : rowMap.keySet()) {
            cols = rowMap.get(row).size();
            if (cols>maxCols) {
                maxCols = cols;
            }
        }

        for(Integer row : rowMap.keySet()) {
            while (rowMap.get(row).size() < maxCols) {
                rowMap.get(row).add("");
            }
        }
    }

//    private ArrayList<CellType> getColumnDatatypesFromRow(Sheet sheet, int rowNumber) {
//        ArrayList<CellType> datatypes = new ArrayList<>();
//        Row row = sheet.getRow(rowNumber);
//
//        for (int colNumber = 0; colNumber < row.getLastCellNum(); colNumber++) {
//            Cell cell = row.getCell(colNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
//            datatypes.add(cell.getCellType());
//        }
//        return datatypes;
//    }

    private int getSelectionColNumber(Map<Integer, String> titles, ExcelSheet excelSheet) {
        String formattedTitle = formatConform(excelSheet.getSelectionColTitle());

        for (int col : titles.keySet()) {
            if(titles.get(col).equals(formattedTitle)) {
                return col;
            }
        }
        return -1;
    }

    private HashMap<Integer, String> extractColTitles(int rowNumber, Map<Integer,ArrayList<String>> rowMap) {
        HashMap<Integer, String> titleMap = new HashMap<>();

        if(rowMap == null || !rowMap.containsKey(rowNumber)) {
            return titleMap;
        }

        ArrayList<String> titleList = rowMap.remove(rowNumber);
        for(int i=1; i<titleList.size(); i++) {
            titleMap.put(i, titleList.get(i));
        }
        return titleMap;
    }

    private HashMap<String, Integer> reverseMap(Map<Integer, String> map) {
        // must be unique titles
        HashMap<String, Integer> newMap = new HashMap<>();
        for(Map.Entry<Integer, String> entry : map.entrySet()){
            newMap.put(entry.getValue(), entry.getKey());
        }
        return newMap;
    }

    private void createNormalizedTitles(Map<Integer, String> titles) {
        Map<Integer, String> replacements = new HashMap<>();
        int emptyCount = 1;

        String title;
        for (int key : titles.keySet()) {
            title = formatConform(titles.get(key));
            if(title.isBlank()) {
                title = "LEER" + emptyCount;
                emptyCount++;
            }

            replacements.put(key, title);
        }

        titles.putAll(replacements);
    }

    private String formatConform(String string) {
        return string.trim();//.replaceAll("[\\\\/:\\*\\?\\\"<>\\|'\\s\\[\\]\\(\\)´`\\^%&\\{\\}+\\-.]", "_");
    }

    private boolean titlesAreUnique(Map<Integer, String> titlesLoadedExcel) {
        Set<String> set = new HashSet<String>();
        for (String each: titlesLoadedExcel.values()) {
            if (!set.add(each)) {
                System.out.println(each);
                return false;
            }
        }
        return true;
    }

    private Map<Integer, SimpleBooleanProperty> getSelectedInitially(ObservableMap<Integer, ArrayList<String>> excelData, int selectionColNr) {
        HashMap<Integer, SimpleBooleanProperty> selectedRows = new HashMap<>();
        for(int rowNr : excelData.keySet()) {
            ArrayList<String> row = excelData.get(rowNr);
            // same as for the checkbox -> not blank == checked
            if(!row.get(selectionColNr).isBlank()) {
                selectedRows.put(rowNr, new SimpleBooleanProperty(true));
            } else {
                selectedRows.put(rowNr, new SimpleBooleanProperty(false));
            }
        }
        return selectedRows;
    }

    private void addColumnsToView(TableView<ObservableList<String>> sheetPreviewTable, Map<Integer, String> titles, ExcelSheet excelSheet) {



        for(Integer col: titles.keySet()) {

            // ignore row index
            if(col == 0) continue;

            if(titles.get(col).equals(excelSheet.getSelectionColTitle())) {

                // add the checkbox column for stockdata
                TableColumn<ObservableList<String>, Boolean> tableCol = new TableColumn<>("Stammdaten");

                tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
                // my assumption -> no content == not selected
                tableCol.setCellValueFactory(row -> {
                    SimpleBooleanProperty sbp = selectedStockDataRows.get(Integer.valueOf(row.getValue().get(0)));
                    sbp.addListener( (o, ov, nv) -> {
                        sbp.set(nv);
                    });
                    return sbp;
                });

                sheetPreviewTable.getColumns().add(tableCol);


                // add the checkbox column for transactions
                // same concept different listener
                tableCol = new TableColumn<>("Transaktionen");
                tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
                // my assumption -> no content == not selected
                tableCol.setCellValueFactory(row -> {
                    SimpleBooleanProperty sbp = selectedTransactionRows.get(Integer.valueOf(row.getValue().get(0)));
                    sbp.addListener( (o, ov, nv) -> {
                        sbp.set(nv);
                    });
                    return sbp;
                });

                sheetPreviewTable.getColumns().add(tableCol);
                continue;
            }

            // normal columns with string content
            TableColumn<ObservableList<String>, String> tableCol = new TableColumn<>(titles.get(col));
            tableCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(col)));
            tableCol.prefWidthProperty().bind(sheetPreviewTable.widthProperty().multiply(0.12));
            tableCol.setSortable(false);
            sheetPreviewTable.getColumns().add(tableCol);
        }
    }

//    #########################
//    Correlation section
//    #########################

    public void fillStockDataCorrelationTable(TableView<ExcelCorrelation> stockDataCorrelationTable, ExcelSheet excelSheet) {

        // add comboboxes...
        prepareCorrelationTable(stockDataCorrelationTable);

        stockColumnRelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockDbCols = new ArrayList<>();

        // using excelSheet.getExcelCorrelations() accesses the excel correlations inside the excelSheet object
        // therefore the values persist until a new db transaction is done
        // therefore i have to fetch them manually
        for (ExcelCorrelation excelCorrelation : excelCorrelationRepository.findAllByExcelSheetId(excelSheet.getId())) {
            if(excelCorrelation.getCorrelationType() == CorrelationType.STOCKDATA) {
                stockColumnRelations.add(excelCorrelation);
                addedStockDbCols.add(excelCorrelation.getDbColTitle());
            }
        }

        // add correlation for missing stock db columns
        // only excel col title+number left to set
        for(StockDataTableColumn stockColumn : stockDataColumnRepository.findAll()) {
            //datum is set automatically
            String name = stockColumn.getName();

            if(!name.equals("datum") && !addedStockDbCols.contains(name)) {
                ExcelCorrelation excelCorrelation = new ExcelCorrelation();
                excelCorrelation.setCorrelationType(CorrelationType.STOCKDATA);
                excelCorrelation.setExcelSheet(excelSheet);
                excelCorrelation.setStockDataTableColumn(stockColumn);
                excelCorrelation.setDbColTitle(stockColumn.getName());

                addedStockDbCols.add(name);
                stockColumnRelations.add(excelCorrelation);
            }
        }

        stockDataCorrelationTable.setMinHeight(stockColumnRelations.size()*32.5);

        stockDataCorrelationTable.getItems().addAll(stockColumnRelations);
    }

    private void constructComboBox(TableColumn<ExcelCorrelation, String> excelColumn, ObservableList<String> comboBoxOptions) {
        excelColumn.setCellFactory(col -> {
            TableCell<ExcelCorrelation, String> cell = new TableCell<>();
            ComboBox<String> comboBox = new ComboBox<>(comboBoxOptions);

            // update value inside the object
            comboBox.valueProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null) {
                    updateCorrelationByComboBox(cell,comboBox,nv);
                }
            });

            comboBox.prefWidthProperty().bind(col.widthProperty().multiply(1));

            // cell.tableRowProperty().addListener((o, ov, nv) -> {
            cell.graphicProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null && cell.getTableRow().getItem().getExcelColTitle() != null) {
                    comboBox.setValue(cell.getTableRow().getItem().getExcelColTitle());
                }
            });

            cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(comboBox));
            return cell;
        });
    }

    private void updateCorrelationByComboBox(TableCell<ExcelCorrelation, String>  cell, ComboBox comboBox, String newValue) {
        if (cell.getTableRow().getItem() != null) {
            ExcelCorrelation correlation = cell.getTableRow().getItem();
            correlation.setExcelColTitle(newValue);
                correlation.setExcelColNumber(titleToExcelIndex.getOrDefault(newValue, -1));
        }
    }

    public ObservableList<String> mapToObservableList(Map<Integer, String> map) {
        ObservableList<String> excelColTitles = FXCollections.observableArrayList();
        excelColTitles.addAll(map.values());
        return excelColTitles;
    }

    private void prepareCorrelationTable(TableView<ExcelCorrelation> table) {
        // could be done better
        // normal program structure guarantees that this is accessed after table load
        ObservableList<String> comboBoxOptions = mapToObservableList(indexToExcelTitle);

        // to undo selection
        comboBoxOptions.add(0, null);


        TableColumn<ExcelCorrelation, String> stockDbColumn = new TableColumn<>("Datenbank Spalten");
        TableColumn<ExcelCorrelation, String> excelColumn = new TableColumn<>("Excel Spalten");

        //stockDbColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        //excelColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        // populate with name from ExcelCorrelation property
        stockDbColumn.setCellValueFactory(new PropertyValueFactory<>("dbColTitle"));

        constructComboBox(excelColumn, comboBoxOptions);

        //excelColumn.setCellValueFactory(new PropertyValueFactory<>("excelColTitle"));

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
        for (ExcelCorrelation excelCorrelation : excelCorrelationRepository.findAllByExcelSheetId(excelSheet.getId())) {
             if(excelCorrelation.getCorrelationType() == CorrelationType.TRANSACTION) {
                 transactionColumnRelations.add(excelCorrelation);
                 addedTransDbCols.add(excelCorrelation.getDbColTitle());
             }
         }



        transactionCorrelationTable.setMinHeight(transactionColumnsWithType.size()*32.5);


        for(String colName : transactionColumnsWithType.keySet()) {
             if(!addedTransDbCols.contains(colName)) {
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

//    #########################
//    Import section
//    #########################

    public int startDataExtraction() {


        if(!isInExtractableState()) return -2;
        if(!correlationsHaveValidState()) return -3;


        int stockExtractionResult = extractStockData();

        // 0-OK, -1-SilentError -> below other error
        // don't break execution if silent
        // only silent and ok can pass
        if(stockExtractionResult < -1) {
            return stockExtractionResult;
        }

        // create stocks if not existing based on prior imported stock data
        stockDataDbManager.createMissingStocks();

        int transactionExtractionResult = extractTransactionData();

        // 0-OK, -1-SilentError -> below other error
        // only ok can pass
        if(transactionExtractionResult != 0) {
            return transactionExtractionResult;
        }

        // transaction ok but stock had a silent error
        if(stockExtractionResult == -1) return -1;
        return 0;
    }

    public int extractStockData() {

        addToLog("##### Start Stammdaten-Import #####\n");

        // execution is not stopped at a silent error but a log message is added
        boolean silentError = false;

        Connection connection = stockDataDbManager.getConnection();

        HashMap<String, PreparedStatement> statements = createStockDataStatements(connection);
        HashMap<String, ColumnDatatype> columnDatatypes = getStockColDbDatatypes();

        if (statements == null) return -4;
        java.sql.Date dateToday = new java.sql.Date(System.currentTimeMillis());

        // go through all rows
        for(int row : excelSheetRows.keySet()) {

            // skip rows if not selected
            if(!(selectedStockDataRows.get(row).get())) {
                //addToLog("INFO: Stammdaten von Zeile: " + row + " ist nicht markiert.");
                continue;
            }

            // the columns for one row
            ArrayList<String> rowData = excelSheetRows.get(row);

            int isinCol = getColNrByName("isin", stockColumnRelations);

            // check if isin valid
            String isin = rowData.get(isinCol);
            if(isin == null || isin.isBlank() || isin.length() >= 50) {
                silentError = true;
                addToLog("FEHLER: Isin der Zeile "+ row +" fehlerhaft oder länger als 50 Zeichen. ->'"+ isin+"'");
                continue;
            }

            // pick one column per relation from row
            for(ExcelCorrelation correlation : stockColumnRelations) {
                String dbColName = correlation.getDbColTitle();

                // continue bcs the key value is not inserted with a prepared statement
                if(dbColName.equals("isin")) continue;

                int correlationColNumber = correlation.getExcelColNumber();
                // -1 is default and can't be set another way meaning it's not set
                if(correlationColNumber == -1) {
                    //addToLog("INFO: Die Spalte '" + dbColName +"' hat keine Zuordnung.");
                    continue;
                };

                String colData = rowData.get(correlationColNumber);
                if(colData.isBlank()) continue;

                ColumnDatatype datatype = columnDatatypes.getOrDefault(dbColName, null);
                if(datatype == null) {
                    silentError = true;
                    addToLog("Fehler: Der Datenbankspalte " + dbColName
                            +" ist kein Datentyp zugeordnet.");
                    continue;
                }

                if(!matchingDataType(datatype, colData)) {
                    silentError = true;
                    addToLog("Fehler: Der Datentyp der Zeile "+row+" in der Spalte '"+correlation.getExcelColTitle()+
                            "', stimmt nicht mit dem der Datenbankspalte "+dbColName+" vom Typ "+datatype.name()+
                            " überein. Zellendaten: '"+colData+"'");
                    continue;
                }

                // statement exists bcs if there is an error at statement creation
                // the program does not reach this line
                PreparedStatement statement = statements.getOrDefault(dbColName, null);

                if(statement == null) {
                    silentError = true;
                    addToLog("Fehler: Sql-Statment für die Spalte '"+correlation.getExcelColTitle()+
                            "' nicht gefunden");
                    continue;
                }

                silentError &= fillStockStatementAddToBatch(isin, dateToday, statement, colData, datatype);
            }

        }

        executeStatements(connection, statements);
        addToLog("\n##### Ende Stammdaten-Import #####\n");

        if(silentError) return -1;
        return 0;
    }

    private int extractTransactionData() {
        addToLog("##### Start Transaktions Import #####\n");

        // execution is not stopped at a silent error but a log message is added
        boolean silentError = false;

        Connection connection = stockDataDbManager.getConnection();

        HashMap<String, PreparedStatement> statements = createTransactionDataStatements(connection);

        if (statements == null) return -4;

        // transactionColumnNames
        int isinCol = getColNrByName("wertpapier_isin", transactionColumnRelations);
        int dateCol = getColNrByName("transaktions_datum", transactionColumnRelations);
        int depotNameCol = getColNrByName("depot_name", transactionColumnRelations);

        // go through all rows
        for(int row : excelSheetRows.keySet()) {
            // skip rows if not selected
            if(!selectedTransactionRows.get(row).get()) continue;

            // the columns for one row
            ArrayList<String> rowData = excelSheetRows.get(row);

            String depotName = rowData.get(depotNameCol);
            if(depotName == null || depotName.isBlank() || depotName.length() >= 50) {
                addToLog("FEHLER: Depotname der Zeile "+ row +" fehlerhaft, leer oder länger als 50 Zeichen.Wert: '"
                        +depotName+ "' ");
                silentError = true;
                continue;
            }

            String isin = rowData.get(isinCol);
            if(isin == null || isin.isBlank() || isin.length() >= 50) {
                addToLog("FEHLER: Isin der Zeile "+ row +" fehlerhaft, leer oder länger als 50 Zeichen. Wert: '"+isin+"'");
                silentError = true;
                continue;
            }

            String date = rowData.get(dateCol);
            if(!matchingDataType(ColumnDatatype.DATE, date)) {
                addToLog("FEHLER: Transaktionsdatum '"+date+"' der Zeile "+ row +" ist fehlerhaft.");
                silentError = true;
                continue;
            }

            Date parsedDate = Date.valueOf(date);

            // stocks are created beforehand
            var stock = stockRepository.findByIsin(isin);
            if(stock.isEmpty()) {
                addToLog("FEHLER: Das passende Wertpapier zur Transaktion aus Zeile "+row+
                        " konnte nicht gefunden werden. Angegebene Isin: '"+isin+"'");
                silentError = true;
                continue;
            }

            // search if the depot already exists or creates a new one
            Depot depot = depotRepository.findByName(depotName).orElse(null);

            if(depot == null) {
                addToLog("INFO: Erstelle Depot mit dem Namen: "+depotName);
                depot = new Depot(depotName);
                depotRepository.save(depot);
            }

            // using autogenerated id after save
            int depotId = depot.getId();


            for(ExcelCorrelation correlation : transactionColumnRelations) {
                String dbColName = correlation.getDbColTitle();

                // already set before don't do it again
                if (dbColName.equals("depot_name") || dbColName.equals("transaktions_datum") || dbColName.equals("wertpapier_isin")) {
                    continue;
                }

                int correlationColNumber = correlation.getExcelColNumber();
                // -1 is default and can't be set another way meaning it's not set
                if(correlationColNumber == -1) {
                    //addToLog("INFO: Die Spalte '" + dbColName +"' hat keine Zuordnung.");
                    continue;
                }

                String colData = rowData.get(correlationColNumber);

                if(colData.isBlank()) continue;

                ColumnDatatype colDatatype = transactionColumnsWithType.getOrDefault(dbColName, ColumnDatatype.INVALID);

                if(!matchingDataType(colDatatype, colData)) {
                    addToLog("FEHLER: Der Wert der Zelle in der Zeile: "+row+" Spalte: '"
                            +correlation.getExcelColTitle()+"' hat nicht den passenden Datentyp für '"
                            +dbColName+"' vom Typ '"+colDatatype+"'. Wert: '"+colData+"'");
                    silentError = true;
                    continue;
                }

                PreparedStatement statement = statements.getOrDefault(dbColName, null);

                if(statement == null) {
                    silentError = true;
                    addToLog("Fehler: Sql-Statment für die Spalte '"+correlation.getExcelColTitle()+
                            "' nicht gefunden");
                    continue;
                }

                silentError &= fillTransactionStatementAddToBatch(depotId, parsedDate, isin, statement, colData, colDatatype);
            }
        }

        executeStatements(connection, statements);

        addToLog("\n##### Ende Transaktions Import #####\n");
        if(silentError) return -1;
        return 0;
    }

    private boolean matchingDataType(ColumnDatatype colDatatype, String colData) {
        if(colData == null || colDatatype == null) {
            return false;
        } else if(colDatatype == ColumnDatatype.INT && colData.matches("^-?[0-9]+(\\.0{5})?$")) {
            // normal format would be "^-?[0-9]+$" but because of
            // String.format("%.5f", cell.getNumericCellValue()).replace(",",".");
            // 5 zeros are added to int
            return true;
        } else if (colDatatype == ColumnDatatype.DOUBLE && colData.matches("^-?[0-9]+((\\.|,)?[0-9]+)?$")) {
            return true;
        } else if (colDatatype == ColumnDatatype.DATE && colData.matches("^[1-9][0-9]{3}\\-[0-9]{2}\\-[0-9]{2}$")) {
            return true;
        } else return colDatatype == ColumnDatatype.TEXT;
    }

    private int getColNrByName(String name, ObservableList<ExcelCorrelation> correlations) {
        for(ExcelCorrelation correlation : correlations) {
            if(correlation.getDbColTitle().equals(name)) {
                return correlation.getExcelColNumber();
            }
        }
        return -1;
    }

    private boolean isInExtractableState() {
        if(excelSheetRows == null || selectedTransactionRows == null || selectedStockDataRows == null ||
                stockColumnRelations.size() == 0 || transactionColumnRelations.size() == 0) {
            return false;
        }
        return true;
    }

    private boolean correlationsHaveValidState() {
        if(getColNrByName("isin", stockColumnRelations) == -1) return false;
        if(getColNrByName("wertpapier_isin", transactionColumnRelations) == -1) return false;
        if(getColNrByName("transaktions_datum", transactionColumnRelations) == -1) return false;
        if(getColNrByName("depot_name", transactionColumnRelations) == -1) return false;

        return true;
    }

    private HashMap<String, ColumnDatatype> getStockColDbDatatypes() {
        HashMap<String, ColumnDatatype> columnDatatypes = new HashMap<>();
        for(StockDataTableColumn column : stockDataColumnRepository.findAll()) {
            columnDatatypes.put(column.getName(), column.getColumnDatatype());
        }
        return columnDatatypes;
    }

    private HashMap<String, PreparedStatement> createStockDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for(StockDataTableColumn column : stockDataColumnRepository.findAll()) {
            try {
                statements.put(column.getName(), getPreparedStockStatement(column.getName(), connection));
            } catch (SQLException e) {
                e.printStackTrace();
                addToLog("FEHLER: Erstellung des Statements fehlgeschlagen. Spalte: '"
                        + column.getName() +"' Datentyp '" + column.getColumnDatatype()+"' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
    }

    private HashMap<String, PreparedStatement> createTransactionDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for(String colName: transactionColumnsWithType.keySet()) {
            ColumnDatatype type = transactionColumnsWithType.get(colName);
            try {
                statements.put(colName, getPreparedTransactionStatement(colName, connection));
            } catch (SQLException e) {
                e.printStackTrace();
                addToLog("FEHLER: Erstellung des Statements fehlgeschlagen. Spalte: '"
                        + colName +"' Datentyp '" + type +"' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
    }

    public PreparedStatement getPreparedTransactionStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO depottransaktion (depot_id, zeitpunkt, wertpapier_isin, "+dbColName+") VALUES(?,?,?,?) "+
                "ON DUPLICATE KEY UPDATE "+dbColName+"=VALUES("+dbColName+");";
        return connection.prepareCall(sql);
    }

    public PreparedStatement getPreparedStockStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO stammdaten (isin, datum, "+dbColName+") VALUES(?,?,?) ON DUPLICATE KEY UPDATE "+
                dbColName+"=VALUES("+dbColName+");";
        return connection.prepareCall(sql);
    }

    private boolean executeStatements(Connection connection, HashMap<String, PreparedStatement> statements) {
        boolean silentError = false;
        try {
            for(PreparedStatement statement : statements.values()) {
                statement.executeBatch();
                statement.close();
            }
            connection.close();
        } catch (SQLException e) {
            silentError = true;
            addToLog("FEHLER: " + e.getMessage() + " _CAUSE_ " + e.getCause());
        }
        return silentError;
    }

    public boolean fillStockStatementAddToBatch(String isin, Date date, PreparedStatement statement,
                                                String data, ColumnDatatype datatype) {

        try {
            statement.setString(1,isin);
            statement.setDate(2,date);

            fillByDataType( datatype, statement, 3, data);

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            addToLog("FEHLER: Bei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            addToLog("FEHLER: Bei dem Parsen des Wertes '"+data+"' in das Format "
                    +datatype.name()+" ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        }
        return true;
    }

    public boolean fillTransactionStatementAddToBatch(int depotId, Date date, String isin,
                                                      PreparedStatement statement, String data,
                                                      ColumnDatatype datatype) {

     try {
            statement.setInt(1,depotId);
            statement.setDate(2,date);
            statement.setString(3,isin);
            fillByDataType( datatype, statement, 4, data);

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            addToLog("FEHLER: Bei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            addToLog("FEHLER: Bei dem Parsen des Wertes '"+data+"' in das Format "
                    +datatype.name()+" ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        }
        return true;
    }

    public void fillByDataType(ColumnDatatype datatype, PreparedStatement statement, int number, String data) throws SQLException {
        switch (datatype) {
            case DATE:
                LocalDate dataToDate = LocalDate.parse(data);
                statement.setDate(number, Date.valueOf(dataToDate));
                break;
            case TEXT:
                statement.setString(number,data);
                break;
            case INT:
                // casting double to int to remove trailing zeros because of
                // String.format("%.5f", cell.getNumericCellValue()).replace(",",".");
                statement.setInt(number, (int) Double.parseDouble(data));
                break;
            case DOUBLE:
                statement.setDouble(number, Double.parseDouble(data));
                break;
            default:
                break;
        }
    }
}
