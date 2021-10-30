package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheetRepository;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ImportTabManagement {

    @Autowired
    private ExcelSheetRepository excelSheetRepository;

    private static Map<Integer, Boolean> selectedStockDataRows;
    // at first they are equal
    private static Map<Integer, Boolean> selectedTransactionRows;

    private static ObservableList<ObservableList<String>> sheetPreviewTableData = FXCollections.observableArrayList();

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
    }

    public Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle("-fx-background-color: FF4A4A;");
        return tooltip;
    }

    public boolean sheetExists(ExcelSheet excelSheet) {
        try {
            File file = new File(excelSheet.getPath());
            return file.exists() && file.isFile();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public Sheet decryptAndGetSheet(ExcelSheet excelSheet) throws EncryptedDocumentException {
        try {
            Workbook wb = WorkbookFactory.create(new File(excelSheet.getPath()), excelSheet.getPassword());
            return wb.getSheetAt(0);
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    public int fillExcelPreview(TableView<ObservableList<String>> sheetPreviewTable, ExcelSheet excelSheet) throws EncryptedDocumentException{
        sheetPreviewTable.getColumns().clear();
        sheetPreviewTable.getItems().clear();

        Sheet sheet;
        try {
            sheet = decryptAndGetSheet(excelSheet);
        } catch (EncryptedDocumentException e) {
            System.out.println(e);
            return -1;
        }

        if (excelSheet.getTitleRow() > sheet.getLastRowNum() || excelSheet.getTitleRow() <= 0) {
            return -2;
        }

        ObservableMap<Integer, ArrayList<String>> excelData = FXCollections.observableMap(new TreeMap<>());
        getExcelSheetData(sheet, excelSheet.getTitleRow(), excelData);
        removeEmptyRows(excelData);
        unifyRows(excelData);
        Map<Integer, String> titles = extractColTitles(excelSheet.getTitleRow()-1, excelData);

        int selectionColNumber = getSelectionColNumber(titles, excelSheet);
        if(selectionColNumber == -1) {
            return -3;
        }

        selectedStockDataRows = getSelectedInitially(excelData, selectionColNumber);
        selectedTransactionRows = new HashMap<>(selectedStockDataRows);

        addColumnsToView(sheetPreviewTable, titles, excelSheet);

        // add rows to data observer
        excelData.forEach((row, rowContent) -> {
            ObservableList<String> tableRow = FXCollections.observableArrayList();
            tableRow.addAll(rowContent);
            sheetPreviewTableData.add(tableRow);
        });

        // add rows themselves
        sheetPreviewTable.setItems(sheetPreviewTableData);
        return 0;
    }

    private void getExcelSheetData(Sheet sheet, int startRow, ObservableMap<Integer, ArrayList<String>> excelData ) {
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
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                excelData.get(rowNumber).add(String.valueOf(cell.getDateCellValue()));
                            } else {
                                excelData.get(rowNumber).add(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case BLANK:
                        case _NONE:

                            excelData.get(rowNumber).add("");
                            break;
                        default:
                            excelData.get(rowNumber).add(cell.getStringCellValue());
                            break;
                    }
                }
            }
        }
    }

    private void removeEmptyRows(Map<Integer, ArrayList<String>> rowMap) {
        // Remove rows which are empty or marked as blank
        List<Integer> rowsToRemove = new ArrayList<>();

        boolean hasContent;

        for(int key : rowMap.keySet()) {
            hasContent = false;
            ArrayList<String> row = rowMap.get(key);

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

        rowsToRemove.forEach(rowMap::remove);
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
        for (int col : titles.keySet()) {
            if(titles.get(col).equals(excelSheet.getSelectionColTitle())) {
                return col;
            }
        }
        return -1;
    }

    private HashMap<Integer, String> extractColTitles(int rowNumber, Map<Integer,ArrayList<String>> rowMap) {
        ArrayList<String> titleList = rowMap.remove(rowNumber);
        HashMap<Integer, String> titleMap = new HashMap<>();
        for(int i=0; i<titleList.size(); i++) {
            titleMap.put(i, titleList.get(i));
        }
        return titleMap;
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
                TableColumn<ObservableList<String>, Boolean> tableCol = new TableColumn<>(titles.get(col));

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
                tableCol = new TableColumn<>("Übernahme Transaktion");
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
            sheetPreviewTable.getColumns().add(tableCol);
        }
    }
}
