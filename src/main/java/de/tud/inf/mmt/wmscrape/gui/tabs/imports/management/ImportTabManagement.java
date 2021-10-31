package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ImportTabManagement {

    @Autowired
    private ExcelSheetRepository excelSheetRepository;
    @Autowired
    private ExcelCorrelationRepository excelCorrelationRepository;
    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;

    private static ObservableList<ObservableList<String>> sheetPreviewTableData = FXCollections.observableArrayList();

    private static Map<Integer, String> titlesOfLoadedExcel;

    // at first they are equal
    private static Map<Integer, Boolean> selectedStockDataRows;
    private static Map<Integer, Boolean> selectedTransactionRows;

    ObservableList<ExcelCorrelation> stockColumnRelations;
    ObservableList<ExcelCorrelation> transactionColumnRelations;

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
            System.out.println(e.getMessage());
            return false;
        }
    }


//    #########################
//
//    Excel parsing section
//
//    #########################


    public Workbook decryptAndGetWorkbook(ExcelSheet excelSheet) throws EncryptedDocumentException {
        try {
            return WorkbookFactory.create(new File(excelSheet.getPath()), excelSheet.getPassword());
        } catch (IOException e) {
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
            // cant decrypt
            return -1;
        }

        if (excelSheet.getTitleRow() > workbook.getSheetAt(0).getLastRowNum() || excelSheet.getTitleRow() <= 0) {
            // column out of bounds
            return -2;
        }

        ObservableMap<Integer, ArrayList<String>> excelData = FXCollections.observableMap(new TreeMap<>());
        boolean evalFaults = getExcelSheetData(workbook, excelSheet.getTitleRow(), excelData);

        try {
            workbook.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if(excelData.size() == 0) {
            return -3;
        }

        removeEmptyRows(excelData);
        unifyRows(excelData);
        removeEmptyCols(excelData, excelSheet);
        titlesOfLoadedExcel = extractColTitles(excelSheet.getTitleRow()-1, excelData);
        createNormalizedTitles(titlesOfLoadedExcel);

        if(!titlesAreUnique(titlesOfLoadedExcel)) {
            return -4;
        }

        int selectionColNumber = getSelectionColNumber(titlesOfLoadedExcel, excelSheet);
        if(selectionColNumber == -1) {
            // selection col not found
            return -5;
        }

        selectedStockDataRows = getSelectedInitially(excelData, selectionColNumber);
        selectedTransactionRows = new HashMap<>(selectedStockDataRows);

        addColumnsToView(sheetPreviewTable, titlesOfLoadedExcel, excelSheet);

        // add rows to data observer
        excelData.forEach((row, rowContent) -> {
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

        Sheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true);
        DataFormatter formatter = new DataFormatter();
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
                                String raw = formatter.formatCellValue(cell);
                                if(!raw.matches("^[0-9]+((\\.|,)[0-9]*)?$")) {
                                    value = raw;
                                } else if (DateUtil.isCellDateFormatted(cell)) {
                                    value = String.valueOf(cell.getDateCellValue());
                                } else {
                                    value = String.valueOf(cell.getNumericCellValue());
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
                        value = "ERROR: Evaluationsfehler: " + cellError[cellError.length-1];
                        evalFault = true;
                    }
                    excelData.get(rowNumber).add(value);
                }
            }
        }
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

    private ArrayList<CellType> getColumnDatatypesFromRow(Sheet sheet, int rowNumber) {
        ArrayList<CellType> datatypes = new ArrayList<>();
        Row row = sheet.getRow(rowNumber);

        for (int colNumber = 0; colNumber < row.getLastCellNum(); colNumber++) {
            Cell cell = row.getCell(colNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            datatypes.add(cell.getCellType());
        }
        return datatypes;
    }

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

    private Map<Integer, Boolean> getSelectedInitially(ObservableMap<Integer, ArrayList<String>> excelData, int selectionColNr) {
        HashMap<Integer, Boolean> selectedRows = new HashMap<>();
        for(int rowNr : excelData.keySet()) {
            ArrayList<String> row = excelData.get(rowNr);
            // same as for the checkbox -> not blank == checked
            if(!row.get(selectionColNr).isBlank()) {
                selectedRows.put(rowNr, true);
            } else {
                selectedRows.put(rowNr, false);
            }
        }
        return selectedRows;
    }

    private void addColumnsToView(TableView<ObservableList<String>> sheetPreviewTable, Map<Integer, String> titles, ExcelSheet excelSheet) {
        for(Integer col: titles.keySet()) {

            if(col == 0) {
                // ignore row index
                continue;
            }

            if(titles.get(col).equals(excelSheet.getSelectionColTitle())) {

                // add the checkbox column for stockdata
                TableColumn<ObservableList<String>, Boolean> tableCol = new TableColumn<>("Stammdaten");

                tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
                // my assumption -> no content == not selected
                tableCol.setCellValueFactory(param -> {
                    SimpleBooleanProperty simpleBooleanProperty =  new SimpleBooleanProperty(!param.getValue().get(col).isBlank());
                    simpleBooleanProperty.addListener( (o, ov, nv) -> {
                        selectedStockDataRows.put(Integer.valueOf(param.getValue().get(0)), nv);
                    });
                    return simpleBooleanProperty;
                });

                sheetPreviewTable.getColumns().add(tableCol);


                // add the checkbox column for transactions
                // same concept different listener
                tableCol = new TableColumn<>("Transaktionen");
                tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
                // my assumption -> no content == not selected
                tableCol.setCellValueFactory(param -> {
                    SimpleBooleanProperty simpleBooleanProperty =  new SimpleBooleanProperty(!param.getValue().get(col).isBlank());
                    simpleBooleanProperty.addListener( (o, ov, nv) -> {
                        selectedTransactionRows.put(Integer.valueOf(param.getValue().get(0)), nv);
                    });
                    return simpleBooleanProperty;
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
//
//    Correlation section
//
//    #########################

    @Transactional
    public void fillStockDataCorrelationTable(TableView<ExcelCorrelation> stockDataCorrelationTable, ExcelSheet excelSheet) {

        // add comboboxes...
        prepareCorrelationTable(stockDataCorrelationTable);

        stockColumnRelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockDbCols = new ArrayList<>();

        // split types between the two tables
        for (ExcelCorrelation excelCorrelation : excelSheet.getExcelCorrelations()) {
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

    private void constructComboBox(TableColumn<ExcelCorrelation, String> excelColumn, ObservableList<String> excelColTitles) {
        excelColumn.setCellFactory(col -> {
            TableCell<ExcelCorrelation, String> cell = new TableCell<>();
            ComboBox<String> comboBox = new ComboBox<>(excelColTitles);

            // update value inside the object
            comboBox.valueProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null) {
                    System.out.println("A " + ov + " , " + nv);
                    cell.getTableRow().getItem().setExcelColTitle(nv);
                }
            });

            comboBox.prefWidthProperty().bind(col.widthProperty().multiply(1));

            // cell.tableRowProperty().addListener((o, ov, nv) -> {
            cell.graphicProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null && cell.getTableRow().getItem().getExcelColTitle() != null) {
                    System.out.println("B " + ov + " , " + nv);
                    comboBox.setValue(cell.getTableRow().getItem().getExcelColTitle());
                }
            });

            cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(comboBox));
            return cell;
        });
    }

    public ObservableList<String> mapToObservableList(Map<Integer, String> map) {
        ObservableList<String> excelColTitles = FXCollections.observableArrayList();
        excelColTitles.addAll(map.values());
        return excelColTitles;
    }

    private void prepareCorrelationTable(TableView<ExcelCorrelation> table) {
        // could be done better
        // normal program structure guarantees that this is accessed after table load
        ObservableList<String> excelColTitles = mapToObservableList(titlesOfLoadedExcel);

        TableColumn<ExcelCorrelation, String> stockDbColumn = new TableColumn<>("Datenbank Spalten");
        TableColumn<ExcelCorrelation, String> excelColumn = new TableColumn<>("Excel Spalten");

        //stockDbColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        //excelColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        // populate with name from ExcelCorrelation property
        stockDbColumn.setCellValueFactory(new PropertyValueFactory<>("dbColTitle"));

        constructComboBox(excelColumn, excelColTitles);

        //excelColumn.setCellValueFactory(new PropertyValueFactory<>("excelColTitle"));

        table.getColumns().add(stockDbColumn);
        table.getColumns().add(excelColumn);
    }

     public void fillTransactionCorrelationTable(TableView<ExcelCorrelation> transactionCorrelationTable, ExcelSheet excelSheet) {

        prepareCorrelationTable(transactionCorrelationTable);

         transactionColumnRelations = FXCollections.observableArrayList();
         ArrayList<String> addedTransDbCols = new ArrayList<>();

         // split types between the two tables
         for (ExcelCorrelation excelCorrelation : excelSheet.getExcelCorrelations()) {
             if(excelCorrelation.getCorrelationType() == CorrelationType.TRANSACTION) {
                 transactionColumnRelations.add(excelCorrelation);
                 addedTransDbCols.add(excelCorrelation.getDbColTitle());
             }
         }

         final String[] transactionColumnNames = {
                 "depot_id", "wertpapier_isin", "zeitpunkt", "tansaktionstyp",
                 "anzahl", "währung", "preis", "wert_in_eur", "bankprovision",
                 "maklercourtage", "börsenplatzgebühr", "spesen", "kapitalertragssteuer",
                 "solidaritätssteuer", "quellensteuer", "abgeltungssteuer", "kirchensteuer"};

         transactionCorrelationTable.setMinHeight(transactionColumnNames.length*32.5);


         for(String colName : transactionColumnNames) {
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

}
