package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

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
import java.util.ArrayList;
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

    private SimpleStringProperty logText;

    public void createNewExcel(String description) {
        excelSheetRepository.save(new ExcelSheet(description));
    }

    public void deleteSpecificExcel(ExcelSheet excelSheet) {
        // fix for not working orphan removal
        excelSheet.setExcelCorrelations(new ArrayList<>());
        excelSheetRepository.delete(excelSheet);
    }

    public ObservableList<ExcelSheet> initExcelSheetList(ListView<ExcelSheet> excelSheetList) {
        ObservableList<ExcelSheet> excelSheetObservableList = FXCollections.observableList(excelSheetRepository.findAll());
        excelSheetList.setItems(excelSheetObservableList);
        return excelSheetObservableList;
    }

    public List<ExcelSheet> getExcelSheets() {
        return excelSheetRepository.findAll();
    }

    public void saveExcel(ExcelSheet excelSheet) {
        excelSheetRepository.save(excelSheet);

        for(ExcelCorrelation excelCorrelation : correlationManager.getStockColumnRelations()) {
            excelCorrelationRepository.save(excelCorrelation);
        }

        for(ExcelCorrelation excelCorrelation : correlationManager.getTransactionColumnRelations()) {
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

    public int fillExcelPreview(TableView<ObservableList<String>> sheetPreviewTable, ExcelSheet excelSheet) throws EncryptedDocumentException{
        return parsingManager.fillExcelPreview(sheetPreviewTable, excelSheet);
    }

    public void fillStockDataCorrelationTable(TableView<ExcelCorrelation> stockDataCorrelationTable, ExcelSheet excelSheet) {
        correlationManager.fillStockDataCorrelationTable(stockDataCorrelationTable, excelSheet);
    }

    public void fillTransactionCorrelationTable(TableView<ExcelCorrelation> transactionCorrelationTable, ExcelSheet excelSheet) {
        correlationManager.fillTransactionCorrelationTable(transactionCorrelationTable, excelSheet);
    }

    public int startDataExtraction() {
        return extractionManager.startDataExtraction();
    }

}
