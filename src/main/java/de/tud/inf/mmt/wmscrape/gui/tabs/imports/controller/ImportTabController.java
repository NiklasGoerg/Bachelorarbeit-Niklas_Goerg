package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.management.ImportTabManagement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class ImportTabController {
    @FXML private ListView<ExcelSheet> excelSheetList;
    @FXML private TextField pathField;
    @FXML private PasswordField passwordField;
    @FXML private TextField titleRowNrField;
    @FXML private TextField selectionColTitleField;
    @FXML private TableView sheetPreviewTable;
    @FXML private TableView stockDataCorrelationTable;
    @FXML private TableView transactionCorrelationTable;

    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private NewExcelPopupController newExcelPopupController;
    @Autowired
    private ImportTabManagement importTabManagement;

    private ObservableList<ExcelSheet> excelSheetObservableList;

    @FXML
    private void initialize() {
        excelSheetObservableList = importTabManagement.initExcelSheetList(excelSheetList);
        excelSheetList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldSheet, newSheet) -> {
                    loadSpecificExcel(newSheet);
                });

        pathField.textProperty().addListener((o,ov,nv) -> validPath());
        passwordField.textProperty().addListener((o,ov,nv) -> validPassword());
        titleRowNrField.textProperty().addListener((o,ov,nv)-> validTitleColNr());
        selectionColTitleField.textProperty().addListener((o,ov,nv) -> validSelectionColTitle());

    }

    @FXML
    private void handleNewExcelSheetButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/imports/controller/newExcelPopup.fxml",
                "Neue Exceltabelle anlegen",
                excelSheetList,
                true, newExcelPopupController);
    }

    @FXML
    private void handleDeleteExcelSheetButton() {
        clearFields();
        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        if(excelSheet == null) {
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Wählen Sie eine Exceldatei aus der Liste aus, um diese zu löschen.",
                    ButtonType.OK);
            alert.setHeaderText("Keine Excel zum löschen ausgewählt!");
            alert.showAndWait();
            return;
        }

        importTabManagement.deleteSpecificExcel(excelSheet);
        reloadExcelList();
    }

    @FXML
    private void saveSpecificExcel() {
        if(!isValidInput()) {
            return;
        }

        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        excelSheet.setPath(pathField.getText());
        excelSheet.setPassword(passwordField.getText());
        excelSheet.setTitleRow(Integer.parseInt(titleRowNrField.getText()));
        excelSheet.setSelectionColTitle(selectionColTitleField.getText());

        importTabManagement.saveExcel(excelSheet);
    }

    @FXML
    private void handleFileSelectionButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Sheet", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(excelSheetList.getScene().getWindow());

        if(selectedFile != null) {
            pathField.setText(selectedFile.getPath());
        }
    }

    public void selectLastExcel() {
        excelSheetList.getSelectionModel().selectLast();
    }

    public void reloadExcelList() {
        excelSheetObservableList.clear();
        excelSheetObservableList.addAll(importTabManagement.getExcelSheets());
    }

    private void clearFields() {
        pathField.clear();
        passwordField.clear();
        titleRowNrField.clear();
        selectionColTitleField.clear();
        sheetPreviewTable.getItems().clear();
        stockDataCorrelationTable.getItems().clear();
        transactionCorrelationTable.getItems().clear();
    }

    private void loadSpecificExcel(ExcelSheet excelSheet) {
        if (excelSheet == null) {
            return;
        }
        pathField.setText(excelSheet.getPath());
        passwordField.setText(excelSheet.getPassword());
        titleRowNrField.setText(String.valueOf(excelSheet.getTitleRow()));
        selectionColTitleField.setText(excelSheet.getSelectionColTitle());

        sheetPreviewTable.getItems().clear();
        stockDataCorrelationTable.getItems().clear();
        transactionCorrelationTable.getItems().clear();
    }

    private boolean validPath() {
        pathField.getStyleClass().remove("bad-input");
        pathField.setTooltip(new Tooltip(""));

        if(pathField.getText().isBlank()) {
            pathField.setTooltip(createTooltip("Dieses Feld darf nicht leer sein!"));
            pathField.getStyleClass().add("bad-input");
            return false;
        } else if (!pathField.getText().matches("^.*\\.xlsx$")) {
            pathField.setTooltip(createTooltip("Dieses Feld darf nur auf xlsx Dateien verweisen!"));
            pathField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }

    private boolean validPassword() {
        passwordField.getStyleClass().remove("bad-input");
        passwordField.setTooltip(new Tooltip(""));

        if(passwordField.getText().isBlank()) {
            passwordField.setTooltip(createTooltip("Dieses Feld darf nicht leer sein!"));
            passwordField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }

    private boolean validTitleColNr() {
        titleRowNrField.getStyleClass().remove("bad-input");
        titleRowNrField.setTooltip(new Tooltip(""));

        if(titleRowNrField.getText().isBlank()) {
            titleRowNrField.setTooltip(createTooltip("Dieses Feld darf nicht leer sein!"));
            titleRowNrField.getStyleClass().add("bad-input");
            return false;
        } else if (!titleRowNrField.getText().matches("^[0-9]+$")) {
            titleRowNrField.setTooltip(createTooltip("Dieses Feld darf nur eine kombination der Zahlen 0-9 enthalten!"));
            titleRowNrField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }

    private boolean validSelectionColTitle() {
        selectionColTitleField.getStyleClass().remove("bad-input");
        selectionColTitleField.setTooltip(new Tooltip(""));

        if(selectionColTitleField.getText().isBlank()) {
            selectionColTitleField.setTooltip(createTooltip("Dieses Feld darf nicht leer sein!"));
            selectionColTitleField.getStyleClass().add("bad-input");
            return false;
        }

        return true;
    }

    private boolean isValidInput() {
        return validPath() && validPassword() && validTitleColNr() && validSelectionColTitle();
    }

    private Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle("-fx-background-color: FF4A4A;");
        return tooltip;
    }
}
