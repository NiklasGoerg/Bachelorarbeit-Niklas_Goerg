package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheetRepository;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImportTabManager {

    @Autowired
    private ExtractionManager extractionManager;
    @Autowired
    private CorrelationManager correlationManager;
    @Autowired
    private ParsingManager parsingManager;
    @Autowired
    private ExcelSheetRepository excelSheetRepository;
    @Autowired
    private ExcelCorrelationRepository excelCorrelationRepository;
    @Autowired
    private ImportTabController importTabController;

    private SimpleStringProperty logText;

    private Task<Integer> currentTask = null;

    public void createNewExcel(String description) {
        excelSheetRepository.save(new ExcelSheet(description));
    }

    public void deleteSpecificExcel(ExcelSheet excelSheet) {
        excelSheetRepository.delete(excelSheet);
    }

    /**
     * fills the excel configuration selection list inside the import tab
     *
     * @param excelSheetList the javafx list object
     * @return a list of all excel configurations
     */
    public ObservableList<ExcelSheet> initExcelSheetList(ListView<ExcelSheet> excelSheetList) {
        ObservableList<ExcelSheet> excelSheetObservableList = FXCollections.observableList(excelSheetRepository.findAll());
        excelSheetList.setItems(excelSheetObservableList);
        return excelSheetObservableList;
    }

    public List<ExcelSheet> getExcelSheets() {
        return excelSheetRepository.findAll();
    }

    /**
     * note that the flush in saveAndFlush is necessary because otherwise it can happen that no excel configuration
     * has been persisted yet but correlations refer to them (constraint error)
     *
     * @param excelSheet the excel configuration
     */
    public void saveExcelConfig(ExcelSheet excelSheet) {
        excelSheetRepository.saveAndFlush(excelSheet);

        for(ExcelCorrelation excelCorrelation : importTabController.getStockDataCorrelations()) {
            excelCorrelationRepository.save(excelCorrelation);
        }

        for(ExcelCorrelation excelCorrelation : importTabController.getTransactionCorrelations()) {
            excelCorrelationRepository.save(excelCorrelation);
        }
    }

    /**
     * checks if an excel sheet file exsits under the set path in the excel configuration
     *
     * @param excelSheet the excel configuration
     * @return true if it exists
     */
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

    /**
     * used to add messages from the parser and scraper to the log window
     *
     * @param line the message text
     */
    public void addToLog(String line) {
        logText.set(this.logText.getValue() +"\n" + line);
    }

    /**
     * starts the excel parsing and fills the preview with the extracted data
     *
     * @param sheetPreviewTable the javaFX preview table
     * @param excelSheet the configuration used for the parsing
     */
    public void fillExcelPreview(TableView<List<String>> sheetPreviewTable, ExcelSheet excelSheet){
        sheetPreviewTable.getColumns().clear();
        sheetPreviewTable.getItems().clear();

        // ignore action if task is running
        if(currentTask != null && (currentTask.getState() == Worker.State.RUNNING ||
                currentTask.getState() == Worker.State.SCHEDULED)) return;

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return parsingManager.parseExcel(excelSheet, this);
            }
        };

        task.setOnSucceeded(event -> importTabController.onPreviewTaskFinished(task.getValue()));
        task.setOnCancelled(event -> importTabController.onPreviewTaskFinished(-9));
        task.setOnFailed(event -> importTabController.onPreviewTaskFinished(-10));
        startTask(task);
    }

    private void startTask(Task<Integer> task) {
        task.exceptionProperty().addListener((o, ov, nv) ->  {
            if(nv != null) {
                Exception e = (Exception) nv;
                System.out.println(e.getMessage()+" "+e.getCause());
            }
        });

        currentTask = task;

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    public void cancelTask() {
        if(currentTask == null) return;
        currentTask.cancel();
    }

    /**
     * prepares the row preview table including the checkboxes
     *
     * @param sheetPreviewTable the javafx preview table
     * @param excelSheet the excel configuration
     */
    public void preparePreviewTable(TableView<List<String>> sheetPreviewTable, ExcelSheet excelSheet) {
        Map<Integer, String> titles = parsingManager.getIndexToExcelTitle();

        for (Integer col : titles.keySet()) {

            // ignore row index
            if (col == 0) continue;

            if (titles.get(col).equals(excelSheet.getSelectionColTitle())) {
                setColumnCheckboxFactory(sheetPreviewTable, "Stammdaten", parsingManager.getSelectedStockDataRows());
                setColumnCheckboxFactory(sheetPreviewTable, "Transaktionen", parsingManager.getSelectedTransactionRows());
                continue;
            }

            // normal columns with string content
            TableColumn<List<String>, String> tableCol = new TableColumn<>(titles.get(col));
            tableCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(col)));
            tableCol.prefWidthProperty().bind(sheetPreviewTable.widthProperty().multiply(0.12));
            //tableCol.setSortable(false);
            sheetPreviewTable.getColumns().add(tableCol);
        }
    }

    public void fillPreviewTable(TableView<List<String>> sheetPreviewTable) {
        // adding the content as list (converting from map)
        sheetPreviewTable.getItems().addAll(new ArrayList<>(parsingManager.getExcelSheetRows().values()));
    }

    /**
     * adds a checkbox column the table
     *
     * @param sheetPreviewTable the table to add the column to
     * @param colName the column header name
     * @param selected the previously extracted selection of selected rows
     */
    private void setColumnCheckboxFactory(TableView<List<String>> sheetPreviewTable, String colName,
                                          Map<Integer, SimpleBooleanProperty> selected) {
        // add a checkbox column
        TableColumn<List<String>, Boolean> tableCol = new TableColumn<>(colName);
        tableCol.setCellFactory(CheckBoxTableCell.forTableColumn(tableCol));
        tableCol.setCellValueFactory(row -> selected.get(Integer.valueOf(row.getValue().get(0))));

        sheetPreviewTable.getColumns().add(tableCol);
    }

    /**
     * starts the data extraction
     *
     * @return a value indicating errors. 0 equals no error
     */
    public int startDataExtraction() {
        if (!isInExtractableState()) return -2;
        if (!correlationsHaveValidState()) return -3;

        return extractionManager.startDataExtraction();
    }

    /**
     * basic test if the preview was loaded by checking if tables are empty
     *
     * @return true if extraction/import can begin
     */
    private boolean isInExtractableState() {
        return parsingManager.getExcelSheetRows() != null &&
                parsingManager.getSelectedTransactionRows() != null &&
                parsingManager.getSelectedStockDataRows() != null &&
                importTabController.getStockDataCorrelations().size() != 0 &&
                importTabController.getTransactionCorrelations().size() != 0;
    }

    /**
     * checks if the necessary primary key correlations have been set
     *
     * @return true if all necessary correlations are set
     */
    private boolean correlationsHaveValidState() {
        if (incorrectStockCorr("isin") || incorrectStockCorr("wkn") ||
                incorrectStockCorr("name")) return false;

        return correctTransactionCorr("wertpapier_isin") && correctTransactionCorr("transaktions_datum") &&
                correctTransactionCorr("transaktionstyp") && correctTransactionCorr("depot_name");
    }

    private boolean correctTransactionCorr(String name) {
        return parsingManager.getColNrByName(name, importTabController.getTransactionCorrelations()) != -1;
    }

    private boolean incorrectStockCorr(String name) {
        return parsingManager.getColNrByName(name, importTabController.getStockDataCorrelations()) == -1;
    }

}
