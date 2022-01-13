package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Lazy
public class ParsingManager {
    
    @Autowired
    private ImportTabManager importTabManager;

    private final ObservableMap<Integer, ArrayList<String>> excelSheetRows = FXCollections.observableMap(new TreeMap<>());

    private Map<Integer, String> indexToExcelTitle;
    private Map<String, Integer> titleToExcelIndex;

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


    /**
     * using the parameters inside the Excel configuration to open and decrypt the Excel sheet file
     *
     * @param excelSheet the Excel configuration
     */
    private XSSFWorkbook decryptAndGetWorkbook(ExcelSheet excelSheet) throws EncryptedDocumentException {
        try {
            return (new XSSFWorkbookFactory()).create(new File(excelSheet.getPath()), excelSheet.getPassword(), true);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * does all the necessary operations to process the Excel sheet
     *
     * @param excelSheet the Excel configuration
     * @param task the task the function is running is. used to react to task cancellation
     * @return the integer return code representing errors
     */
    public int parseExcel(ExcelSheet excelSheet, Task<Integer> task) {

        XSSFWorkbook workbook;
        try {
            workbook = decryptAndGetWorkbook(excelSheet);
        } catch (EncryptedDocumentException e) {
            //e.printStackTrace();
            // cant decrypt
            return -1;
        }

        if(workbook == null) {
            return -1;
        }

        if (excelSheet.getTitleRow() > workbook.getSheetAt(0).getLastRowNum() || excelSheet.getTitleRow() <= 0) {
            // column out of bounds
            return -2;
        }

        excelSheetRows.clear();
        boolean evalFaults = getExcelSheetData(workbook, excelSheet.getTitleRow(), excelSheetRows, task);

        // return value doesn't matter because canceling is canted extra
        if(task.isCancelled()) return -10;

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (excelSheetRows.isEmpty()) return -3;

        removeEmptyRows(excelSheetRows);

        // title row is empty -> invalid
        if(!excelSheetRows.containsKey(excelSheet.getTitleRow() - 1)) return -4;

        unifyRows(excelSheetRows);
        removeEmptyCols(excelSheetRows, excelSheet);
        indexToExcelTitle = extractColTitles(excelSheet.getTitleRow() - 1, excelSheetRows);

        createNormalizedTitles(indexToExcelTitle);
        // checks that every title is unique
        if (!titlesAreUnique(indexToExcelTitle)) return -5;

        titleToExcelIndex = reverseMap(indexToExcelTitle);

        int selectionColNumber = getColNumberByName(indexToExcelTitle, excelSheet.getSelectionColTitle());
        // selection col not found
        if (selectionColNumber == -1) return -6;

        int depotColNumber = getColNumberByName(indexToExcelTitle, excelSheet.getDepotColTitle());
        // depot col not found
        if (depotColNumber == -1) return -7;

        selectedStockDataRows = getSelectedInitially(excelSheetRows, selectionColNumber, true);
        selectedTransactionRows = getSelectedInitially(excelSheetRows, depotColNumber, false);

        if (evalFaults) return -8;
        return 0;
    }

    /**
     * extracts the Excel sheet information cell by cell and stores it temporarily.
     *
     * @param workbook the apache poi representation of an opened Excel sheet file
     * @param startRow the row containing the Excel sheet titles (note: that poi index starts with 0 excel with 1)
     * @param excelData the list where the data is stored
     * @return returns true if there were errors while parsing the file
     */
    private boolean getExcelSheetData(XSSFWorkbook workbook, int startRow, ObservableMap<Integer,
                                        ArrayList<String>> excelData, Task<Integer> task) {

        importTabManager.addToLog("##### Start Excel Parsing #####\n");

        XSSFSheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        evaluator.setIgnoreMissingWorkbooks(true); // throw no error if some formulas reference another unreachable sheet
        boolean evalFault = false;
        int lastRowNr = sheet.getLastRowNum();

        // for each table row
        // excel starts with index 1 "poi" with 0
        for (int rowNumber = startRow-1; rowNumber < lastRowNr; rowNumber++) {
            if(task.isCancelled()) return false;

            XSSFRow row = sheet.getRow(rowNumber);

            // skip if null
            if (row == null) continue;

            // for each column per row
            for (int colNumber = 0; colNumber < row.getLastCellNum(); colNumber++) {
                if(task.isCancelled()) return false;

                XSSFCell cell = row.getCell(colNumber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String stringValue = "";

                // add new array if new row
                if (!excelData.containsKey(rowNumber)) {
                    excelData.put(rowNumber, new ArrayList<>());
                    // add row index
                    excelData.get(rowNumber).add(String.valueOf(rowNumber));
                }

                // cell stringValue processing
                if (cell == null) {
                    excelData.get(rowNumber).add("");
                } else {
                    try {

                        // evaluate formulas
                        CellValue cellValue = evaluator.evaluate(cell);

                        switch (cellValue.getCellType()) {
                            case STRING:
                                stringValue = cellValue.getStringValue();
                                break;
                            case NUMERIC:
                                if (cell.getCellStyle().getDataFormatString().contains("%")) {
                                    // percent format. excel stores them as decimal 0.1 == 10%
                                    stringValue = String.format("%.6f", cellValue.getNumberValue()*100).replace(",", ".");
                                } else if (DateUtil.isCellDateFormatted(cell)) {
                                    // copied from org.apache.poi.xssf.usermodel.XSSFCell # getDateCellValue()
                                    // because cell.getDateCellValue().getTime() is not offered for CellValue
                                    double value = cellValue.getNumberValue();
                                    boolean date1904 = cell.getSheet().getWorkbook().isDate1904();
                                    Date date = DateUtil.getJavaDate(value, date1904);

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    stringValue = dateFormat.format(date);
                                } else {
                                    stringValue = String.format("%.6f", cellValue.getNumberValue()).replace(",", ".");
                                }
                                break;
                            case BOOLEAN:
                                stringValue = String.valueOf(cellValue.getBooleanValue());
                                break;
                            // commented out bcs. it makes no sense to include the error codes. like NaN is code 42
                            // which would be set instead and imported
                            //case ERROR:
                            //    stringValue = String.valueOf(cellValue.getErrorValue());
                            //    break;
                            default:
                                stringValue = "";
                                break;
                        }
                    } catch (NotImplementedException e) {
                        // TODO: http://poi.apache.org/components/spreadsheet/user-defined-functions.html
                        //      https://poi.apache.org/components/spreadsheet/eval-devguide.html (bottom)
                        // example org.apache.poi.ss.formula.eval.NotImplementedException: Error evaluating cell 'WP Depot'!AX75
                        System.out.println(e.getMessage());
                        importTabManager.addToLog(e.getMessage() + " _CAUSE:_ " + e.getCause());
                        evalFault = true;
                    } catch (Exception e) {
                        importTabManager.addToLog("Unbekannter Fehler in Zeile: "+rowNumber+" Spalte: "
                                +colNumber+" Nachricht: "+e.getMessage()+"    _GRUND_    "+e.getCause());
                        evalFault = true;
                    }

                    excelData.get(rowNumber).add(stringValue.trim());
                }
            }
        }

        importTabManager.addToLog("##### Ende Excel Parsing #####\n");
        return evalFault;
    }

    /**
     * Remove rows which are empty
     *
     * @param excelData all Excel sheet data rows mapped to the row index
     */
    private void removeEmptyRows(Map<Integer, ArrayList<String>> excelData) {
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

    /**
     * removes empty columns.
     * note: the list has to be unified beforehand ({@link #unifyRows(Map)}
     *
     * @param rowMap all Excel sheet data rows mapped to the row index
     * @param excelSheet the Excel configuration
     */
    private void removeEmptyCols(Map<Integer, ArrayList<String>> rowMap, ExcelSheet excelSheet) {

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

    /**
     * adds columns to create uniform rows of the same length
     *
     * @param rowMap all Excel sheet data rows mapped to the row index
     */
    private void unifyRows(Map<Integer, ArrayList<String>> rowMap) {
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

    /**
     * looks up all correlations to find the one witch the matching title.
     *
     * @param title the Excel column name
     * @param titles all titles mapped to the column id inside the Excel sheet file
     * @return the column index of the Excel sheet if found, otherwise -1
     */
    private int getColNumberByName(Map<Integer, String> titles, String title) {
        if(title == null | titles == null) return -1;

        for (int col : titles.keySet()) {
            if (titles.getOrDefault(col, "").equals(title.trim())) {
                return col;
            }
        }
        return -1;
    }

    /**
     * extract the Excel column titles given the row number
     *
     * @param rowNumber the index of the row containing the titles
     * @param rows the list with the titles in order
     * @return a map containing all titles mapped to the column index
     */
    private HashMap<Integer, String> extractColTitles(int rowNumber, Map<Integer, ArrayList<String>> rows) {
        HashMap<Integer, String> titleMap = new HashMap<>();

        if (rows == null || !rows.containsKey(rowNumber)) {
            return titleMap;
        }

        ArrayList<String> titleList = rows.remove(rowNumber);
        for (int i = 1; i < titleList.size(); i++) {
            titleMap.put(i, titleList.get(i));
        }
        return titleMap;
    }

    /**
     *
     * @param map a map containing all titles mapped to the column index
     * @return a map containing all titles mapped to the column index in reverse
     */
    private HashMap<String, Integer> reverseMap(Map<Integer, String> map) {
        // must be unique titles
        HashMap<String, Integer> newMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            newMap.put(entry.getValue(), entry.getKey());
        }
        return newMap;
    }

    /**
     * handle malformed titles. at the moment only empty titles are replaced with "LEER"+NumberOfOccurrence
     *
     * @param titles a map containing all titles mapped to the column index
     */
    private void createNormalizedTitles(Map<Integer, String> titles) {
        Map<Integer, String> replacements = new HashMap<>();
        int emptyCount = 1;

        String title;
        for (int key : titles.keySet()) {
            title = titles.get(key).trim();
            if (title.isBlank()) {
                title = "LEER" + emptyCount;
                emptyCount++;
            }

            replacements.put(key, title);
        }

        titles.putAll(replacements);
    }

    /**
     * non-unique titles would be problematic if one has to choose from the Combo-Box inside the javafx correlation table
     * between columns with the same name.
     *
     * @param titles a map containing all titles mapped to the column index
     * @return true if there are no duplicate titles
     */
    private boolean titlesAreUnique(Map<Integer, String> titles) {
        Set<String> set = new HashSet<>();
        var unique = true;
        for (String title : titles.values()) {
            if (!set.add(title)) {
                importTabManager.addToLog("ERR:\t\t Titel mehrfach vorhanden: "+title);
                unique = false;
            }
        }
        return unique;
    }

    /**
     * selects those rows, that are marked inside the Excel sheet for import
     *
     * @param excelData all extracted Excel sheet rows
     * @param selectionColNr the column index which defines if they should be imported
     * @param removeUnselected if true, not marked rows are deleted from the list
     * @return a mapping between the row index and a boolean property. this property is used for the
     * checkboxes in the javafx preview table and can be changed
     */
    private Map<Integer, SimpleBooleanProperty> getSelectedInitially(ObservableMap<Integer, ArrayList<String>> excelData,
                                                                     int selectionColNr, boolean removeUnselected) {
        HashMap<Integer, SimpleBooleanProperty> selectedRows = new HashMap<>();
        List<Integer> toDelete =  new ArrayList<>();

        for (int rowNr : excelData.keySet()) {
            ArrayList<String> row = excelData.get(rowNr);
            // same as for the checkbox -> not blank == checked
            if (!row.get(selectionColNr).isBlank()) {
                selectedRows.put(rowNr, new SimpleBooleanProperty(true));
            } else if(!removeUnselected){
                selectedRows.put(rowNr, new SimpleBooleanProperty(false));
            } else {
                toDelete.add(rowNr);
            }
        }

        for (int rowNr : toDelete) excelData.remove(rowNr);
        return selectedRows;
    }

    /**
     * looks up all correlations to find the one with the matching title.
     *
     * @param name the Excel column name
     * @param correlations all correlations for the Excel configuration
     * @return the column index of the Excel sheet if found, otherwise -1
     */
    public int getColNrByName(String name, ObservableList<ExcelCorrelation> correlations) {
        for (ExcelCorrelation correlation : correlations) {
            if (correlation.getDbColTitle().equals(name)) {
                return correlation.getExcelColNumber();
            }
        }
        return -1;
    }

}