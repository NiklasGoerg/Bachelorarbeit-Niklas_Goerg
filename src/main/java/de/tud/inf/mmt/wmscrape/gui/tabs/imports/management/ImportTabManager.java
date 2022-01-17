package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheetRepository;
import javafx.application.Platform;
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
     * fills the Excel configuration selection list inside the import tab
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
     * note that the flush in saveAndFlush is necessary because otherwise it can happen that no excels configuration
     * has been persisted yet but correlations refer to them (constraint error)
     *
     * @param excelSheet the Excel configuration
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
     * checks if an Excel sheet file exists under the set path in the Excel configuration
     *
     * @param excelSheet the Excel configuration
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
        Platform.runLater(() -> logText.set(this.logText.getValue() +"\n" + line));
    }

    /**
     * starts the Excel parsing by creating a separate task where the actual process is running in
     *
     * @param sheetPreviewTable the javaFX preview table
     * @param excelSheet the configuration used for the parsing
     */
    public void fillExcelPreview(TableView<List<String>> sheetPreviewTable, ExcelSheet excelSheet){
        sheetPreviewTable.getColumns().clear();
        sheetPreviewTable.getItems().clear();

        // ignore action if task is running
        if(taskIsActive()) return;

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return parsingManager.parseExcel(excelSheet, this);
            }
        };

        task.setOnSucceeded(event -> importTabController.onPreviewTaskFinished(task.getValue()));
        task.setOnCancelled(event -> importTabController.onPreviewTaskFinished(-10));
        task.setOnFailed(event -> importTabController.onPreviewTaskFinished(-11));
        startTask(task);
    }

    /**
     * used to disallow creating multiple task at once
     *
     * @return true if a task is currently active
     */
    private boolean taskIsActive() {
        return currentTask != null && (currentTask.getState() == Worker.State.RUNNING ||
                currentTask.getState() == Worker.State.SCHEDULED);
    }

    /**
     * creates a new thread and starts the task within
     *
     * @param task the task to run
     */
    private void startTask(Task<Integer> task) {
        task.exceptionProperty().addListener((o, ov, nv) ->  {
            if(nv != null) {
                Exception e = (Exception) nv;
                e.printStackTrace();
            }
        });

        currentTask = task;

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    /**
     * stops the current task. needs the checks "isCanceled" inside the task functions to work.
     */
    public void cancelTask() {
        if(currentTask == null) return;
        currentTask.cancel();
    }

    /**
     * prepares the row preview table including the checkboxes
     *
     * @param sheetPreviewTable the javafx preview table
     * @param excelSheet the Excel configuration
     */
    public void preparePreviewTable(TableView<List<String>> sheetPreviewTable, ExcelSheet excelSheet) {
        Map<Integer, String> titles = parsingManager.getIndexToExcelTitle();

        setColumnCheckboxFactory(sheetPreviewTable, "TBD.", parsingManager.getSelectedPreviewRows());
        setColumnCheckboxFactory(sheetPreviewTable, "Stammdaten", parsingManager.getSelectedStockDataRows());
        setColumnCheckboxFactory(sheetPreviewTable, "Transaktionen", parsingManager.getSelectedTransactionRows());

        for (Integer col : titles.keySet()) {

            // ignore row index
            if (col == 0) continue;

            if (titles.get(col).equals(excelSheet.getPreviewSelectionColTitle()) ||
                    titles.get(col).equals(excelSheet.getStockSelectionColTitle()) ||
                    titles.get(col).equals(excelSheet.getTransactionSelectionColTitle())) {
                continue;
            }

            // normal columns with string content
            TableColumn<List<String>, String> tableCol = new TableColumn<>(titles.get(col));
            tableCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(col)));
            tableCol.prefWidthProperty().bind(sheetPreviewTable.widthProperty().multiply(0.12));
            sheetPreviewTable.getColumns().add(tableCol);
        }
    }

    /**
     * adds the row data to the prepared table
     *
     * @param sheetPreviewTable the javafx preview table
     */
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
     * starts the data extraction by creating a separate task where the actual process is running in
     */
    public void startDataExtraction() {
        // ignore action if task is running
        if(taskIsActive()) return;

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return extractionManager.startDataExtraction(this);
            }
        };

        task.setOnSucceeded(event -> importTabController.onImportTaskFinished(task.getValue()));
        task.setOnCancelled(event -> importTabController.onImportTaskFinished(-3));
        task.setOnFailed(event -> importTabController.onImportTaskFinished(-4));
        startTask(task);
    }

    public boolean excelHasContentForImport() {
        return !parsingManager.getExcelSheetRows().isEmpty();
    }

    /**
     * checks if the necessary primary key correlations have been set
     *
     * @return true if all necessary correlations are set
     */
    public boolean correlationsHaveValidState() {
        if (incorrectStockCorr("isin") || incorrectStockCorr("datum")) return false;

        return correctTransactionCorr("wertpapier_isin") && correctTransactionCorr("transaktions_datum") &&
                correctTransactionCorr("transaktionstyp") && correctTransactionCorr("depot_name");
    }

    /**
     * checks if some value has been set to the correlation. -1 is the default value indicating nothing was/is set
     *
     * @param name the name of the database column name
     * @return true if set
     */
    private boolean correctTransactionCorr(String name) {
        return parsingManager.getColNrByName(name, importTabController.getTransactionCorrelations()) != -1;
    }

    /**
     * checks if some value has been set to the correlation. -1 is the default value indicating nothing was/is set
     *
     * @param name the name of the database column name
     * @return true if set
     */
    private boolean incorrectStockCorr(@SuppressWarnings("SameParameterValue") String name) {
        return parsingManager.getColNrByName(name, importTabController.getStockDataCorrelations()) == -1;
    }

}
