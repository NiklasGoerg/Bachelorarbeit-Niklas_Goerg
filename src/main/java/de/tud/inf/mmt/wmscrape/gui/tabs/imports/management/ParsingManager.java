package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ParsingManager {
    
    @Autowired
    private ImportTabManager importTabManager;

    private ObservableList<ObservableList<String>> sheetPreviewTableData = FXCollections.observableArrayList();

    private ObservableMap<Integer, ArrayList<String>> excelSheetRows;

    private Map<Integer, String> indexToExcelTitle;
    private Map<String, Integer> titleToExcelIndex;

    // at first, they are equal
    private Map<Integer, SimpleBooleanProperty> selectedStockDataRows;
    private Map<Integer, SimpleBooleanProperty> selectedTransactionRows;

    public ObservableMap<Integer, ArrayList<String>> getExcelSheetRows() {
        return excelSheetRows;
    }

    public Map<Integer, String> getIndexToExcelTitle() {
        return indexToExcelTitle;
    }

    public Map<String, Integer> getTitleToExcelIndex() {
        return titleToExcelIndex;
    }

    public Map<Integer, SimpleBooleanProperty> getSelectedStockDataRows() {
        return selectedStockDataRows;
    }

    public Map<Integer, SimpleBooleanProperty> getSelectedTransactionRows() {
        return selectedTransactionRows;
    }

    public Workbook decryptAndGetWorkbook(ExcelSheet excelSheet) throws EncryptedDocumentException {
        try {
            return WorkbookFactory.create(new File(excelSheet.getPath()), excelSheet.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int fillExcelPreview(TableView<ObservableList<String>> sheetPreviewTable, ExcelSheet excelSheet) throws EncryptedDocumentException {
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

        if (excelSheetRows.size() == 0) {
            return -3;
        }

        removeEmptyRows(excelSheetRows);
        unifyRows(excelSheetRows);
        removeEmptyCols(excelSheetRows, excelSheet);
        indexToExcelTitle = extractColTitles(excelSheet.getTitleRow() - 1, excelSheetRows);

        createNormalizedTitles(indexToExcelTitle);
        if (!titlesAreUnique(indexToExcelTitle)) {
            return -4;
        }

        titleToExcelIndex = reverseMap(indexToExcelTitle);

        int selectionColNumber = getSelectionColNumber(indexToExcelTitle, excelSheet);
        if (selectionColNumber == -1) {
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
        if (evalFaults) {
            return -6;
        }
        return 0;
    }

    boolean getExcelSheetData(Workbook workbook, int startRow, ObservableMap<Integer, ArrayList<String>> excelData) {

        importTabManager.addToLog("##### Start Excel Parsing #####\n");

        Sheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true);
        //DataFormatter formatter = new DataFormatter();
        String value;
        boolean evalFault = false;

        // for each table row
        // excel starts with index 1 "poi" with 0
        for (int rowNumber = startRow - 1; rowNumber < sheet.getLastRowNum(); rowNumber++) {

            Row row = sheet.getRow(rowNumber);

            // skip if null
            if (row == null) continue;

            // for each column per row
            for (int colNumber = 0; colNumber < row.getLastCellNum(); colNumber++) {
                Cell cell = row.getCell(colNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                // add new array if new row
                if (!excelData.containsKey(rowNumber)) {
                    excelData.put(rowNumber, new ArrayList<>());
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
                                    Date date = new Date(cell.getDateCellValue().getTime());
                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    value = dateFormat.format(date);
                                } else {
                                    value = String.format("%.5f", cell.getNumericCellValue()).replace(",", ".");
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
                        importTabManager.addToLog(e.getMessage() + " _CAUSE:_ " + e.getCause());
                        value = "ERROR: Evaluationsfehler: " + cellError[cellError.length - 1];
                        evalFault = true;
                    }
                    excelData.get(rowNumber).add(value);
                }
            }
        }

        importTabManager.addToLog("##### Ende Excel Parsing #####\n");
        return evalFault;
    }

    void removeEmptyRows(Map<Integer, ArrayList<String>> excelData) {
        // Remove rows which are empty
        List<Integer> rowsToRemove = new ArrayList<>();

        boolean hasContent;

        for (int key : excelData.keySet()) {
            hasContent = false;
            ArrayList<String> row = excelData.get(key);

            // skip the first col as it is the row index
            for (int col = 1; col < row.size(); col++) {
                String cellValue = row.get(col);

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

    void removeEmptyCols(Map<Integer, ArrayList<String>> rowMap, ExcelSheet excelSheet) {
        // Lists have to be unified beforehand
        List<Integer> colsToRemove = new ArrayList<>();

        boolean hasContent;

        for (int col = 1; col < rowMap.get(excelSheet.getTitleRow() - 1).size(); col++) {
            hasContent = false;

            for (ArrayList<String> row : rowMap.values()) {
                if (!row.get(col).isBlank()) {
                    hasContent = true;
                    break;
                }
            }
            if (!hasContent) {
                colsToRemove.add(col);
            }
        }

        int counterShrinkage;
        for (int row : rowMap.keySet()) {
            counterShrinkage = 0;
            for (int col : colsToRemove) {
                ArrayList<String> rowData = rowMap.get(row);
                rowData.remove(col - counterShrinkage);
                rowMap.put(row, rowData);
                counterShrinkage++;
            }
        }
    }

    void unifyRows(Map<Integer, ArrayList<String>> rowMap) {
        // Adds columns to create uniform rows of the same length
        int maxCols = 0;
        int cols;

        for (Integer row : rowMap.keySet()) {
            cols = rowMap.get(row).size();
            if (cols > maxCols) {
                maxCols = cols;
            }
        }

        for (Integer row : rowMap.keySet()) {
            while (rowMap.get(row).size() < maxCols) {
                rowMap.get(row).add("");
            }
        }
    }

    int getSelectionColNumber(Map<Integer, String> titles, ExcelSheet excelSheet) {
        String formattedTitle = formatConform(excelSheet.getSelectionColTitle());

        for (int col : titles.keySet()) {
            if (titles.get(col).equals(formattedTitle)) {
                return col;
            }
        }
        return -1;
    }

    HashMap<Integer, String> extractColTitles(int rowNumber, Map<Integer, ArrayList<String>> rowMap) {
        HashMap<Integer, String> titleMap = new HashMap<>();

        if (rowMap == null || !rowMap.containsKey(rowNumber)) {
            return titleMap;
        }

        ArrayList<String> titleList = rowMap.remove(rowNumber);
        for (int i = 1; i < titleList.size(); i++) {
            titleMap.put(i, titleList.get(i));
        }
        return titleMap;
    }

    HashMap<String, Integer> reverseMap(Map<Integer, String> map) {
        // must be unique titles
        HashMap<String, Integer> newMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            newMap.put(entry.getValue(), entry.getKey());
        }
        return newMap;
    }

    void createNormalizedTitles(Map<Integer, String> titles) {
        Map<Integer, String> replacements = new HashMap<>();
        int emptyCount = 1;

        String title;
        for (int key : titles.keySet()) {
            title = formatConform(titles.get(key));
            if (title.isBlank()) {
                title = "LEER" + emptyCount;
                emptyCount++;
            }

            replacements.put(key, title);
        }

        titles.putAll(replacements);
    }

    String formatConform(String string) {
        return string.trim();//.replaceAll("[\\\\/:\\*\\?\\\"<>\\|'\\s\\[\\]\\(\\)´`\\^%&\\{\\}+\\-.]", "_");
    }

    boolean titlesAreUnique(Map<Integer, String> titlesLoadedExcel) {
        Set<String> set = new HashSet<>();
        for (String each : titlesLoadedExcel.values()) {
            if (!set.add(each)) {
                System.out.println(each);
                return false;
            }
        }
        return true;
    }

    Map<Integer, SimpleBooleanProperty> getSelectedInitially(ObservableMap<Integer, ArrayList<String>> excelData, int selectionColNr) {
        HashMap<Integer, SimpleBooleanProperty> selectedRows = new HashMap<>();
        for (int rowNr : excelData.keySet()) {
            ArrayList<String> row = excelData.get(rowNr);
            // same as for the checkbox -> not blank == checked
            if (!row.get(selectionColNr).isBlank()) {
                selectedRows.put(rowNr, new SimpleBooleanProperty(true));
            } else {
                selectedRows.put(rowNr, new SimpleBooleanProperty(false));
            }
        }
        return selectedRows;
    }

    void addColumnsToView(TableView<ObservableList<String>> sheetPreviewTable, Map<Integer, String> titles, ExcelSheet excelSheet) {


        for (Integer col : titles.keySet()) {

            // ignore row index
            if (col == 0) continue;

            if (titles.get(col).equals(excelSheet.getSelectionColTitle())) {

                // add the checkbox column for stockdata
                TableColumn<ObservableList<String>, Boolean> tableCol = new TableColumn<>("Stammdaten");

                tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
                // my assumption -> no content == not selected
                tableCol.setCellValueFactory(row -> {
                    return selectedStockDataRows.get(Integer.valueOf(row.getValue().get(0)));
                    //sbp.addListener( (o, ov, nv) -> sbp.set(nv));
                });

                sheetPreviewTable.getColumns().add(tableCol);


                // add the checkbox column for transactions
                // same concept different listener
                tableCol = new TableColumn<>("Transaktionen");
                tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
                // my assumption -> no content == not selected
                tableCol.setCellValueFactory(row -> {
                    return selectedTransactionRows.get(Integer.valueOf(row.getValue().get(0)));
                    //sbp.addListener( (o, ov, nv) -> sbp.set(nv));
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
}