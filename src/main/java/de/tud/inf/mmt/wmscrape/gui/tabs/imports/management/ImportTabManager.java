package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheetRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import org.apache.poi.EncryptedDocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

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

//        excelCorrelationRepository.flush();
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

    public int fillExcelPreview(TableView<ObservableList<String>> sheetPreviewTable, ExcelSheet excelSheet) throws EncryptedDocumentException{
        return parsingManager.fillExcelPreview(sheetPreviewTable, excelSheet);
    }

    public int startDataExtraction() {
        return extractionManager.startDataExtraction();
    }

}
