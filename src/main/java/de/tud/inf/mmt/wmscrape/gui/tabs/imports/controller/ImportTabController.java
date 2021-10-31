package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.management.ImportTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataColumnRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.Optional;

@Controller
public class ImportTabController {
    @FXML private ListView<ExcelSheet> excelSheetList;
    @FXML private TextField pathField;
    @FXML private PasswordField passwordField;
    @FXML private TextField titleRowNrField;
    @FXML private TextField selectionColTitleField;
    @FXML private TableView<ObservableList<String>> sheetPreviewTable;
    @FXML private TableView<ExcelCorrelation> stockDataCorrelationTable;
    @FXML private TableView<ExcelCorrelation> transactionCorrelationTable;

    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private NewExcelPopupController newExcelPopupController;
    @Autowired
    private ImportTabManagement importTabManagement;

    private ObservableList<ExcelSheet> excelSheetObservableList;

    @Autowired
    StockDataColumnRepository stockDataColumnRepository;
    static ObservableList<ExcelCorrelation> stockDataTableColumns = FXCollections.observableArrayList();

    private TextArea logTextArea;
    private SimpleStringProperty logText;

    @FXML
    private void initialize() {
        excelSheetObservableList = importTabManagement.initExcelSheetList(excelSheetList);
        excelSheetList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldSheet, newSheet) -> {
                    loadSpecificExcel(newSheet);
                });

        pathField.textProperty().addListener((o,ov,nv) -> { if (nv != null) { validPath(); }});
//        passwordField.textProperty().addListener((o,ov,nv) -> { if (nv != null) { validPassword();}});
        titleRowNrField.textProperty().addListener((o,ov,nv)-> { if (nv != null) { validTitleColNr();}});
        selectionColTitleField.textProperty().addListener((o,ov,nv) -> { if (nv != null) { validSelectionColTitle();}});

        logText = new SimpleStringProperty("");
        logTextArea =  new TextArea();
        logTextArea.setPrefSize(350,400);
        logTextArea.textProperty().bind(logText);
        importTabManagement.passLogText(logText);
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Einstellungen löschen?");
        alert.setContentText("Bitte bestätigen Sie, dass sie diese Exceleinstellungen löschen möchten.");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        importTabManagement.deleteSpecificExcel(excelSheet);
        reloadExcelList();
    }

    @FXML
    private void saveSpecificExcel() {
        if(!excelIsSelected() || !isValidInput()) {
            return;
        }

        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        excelSheet.setPath(pathField.getText());
        excelSheet.setPassword(passwordField.getText());
        excelSheet.setTitleRow(Integer.parseInt(titleRowNrField.getText()));
        excelSheet.setSelectionColTitle(selectionColTitleField.getText());

        importTabManagement.saveExcel(excelSheet);

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Die Exceltabelle wurde gespeichert.",
                ButtonType.OK);
        alert.setHeaderText("Daten gespeichert!");
        alert.showAndWait();
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

    @FXML
    private void previewExcel() {
        if(!excelIsSelected() || !isValidInput()) {
            return;
        }

        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();
        if(!importTabManagement.sheetExists(excelSheet)) {
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Unter dem angegebenen Pfad wurde keine gültige Datei gefunden. Speichern Sie bevor Sie die Vorschau laden.",
                    ButtonType.OK);
            alert.setHeaderText("Datei nicht gefunden!");
            alert.showAndWait();
            return;
        }

        int result = importTabManagement.fillExcelPreview(sheetPreviewTable, excelSheet);
        Alert alert;

        switch (result) {
            case -1:
                // wrong password
                alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Das angegebene Passwort ist falsch. Speichern Sie bevor Sie die Vorschau laden.",
                        ButtonType.OK);
                alert.setHeaderText("Falsches Passwort!");
                alert.showAndWait();
                return;
            case -2:
                // TitleRowError
                alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Die Titelzeile liegt außerhalb der Begrenzung.",
                        ButtonType.OK);
                alert.setHeaderText("Fehlerhafte Titelzeile!");
                alert.showAndWait();
                return;
            case -3:
                // no data in sheet
                alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Die angegebene Datei enhält keine Daten.",
                        ButtonType.OK);
                alert.setHeaderText("Keine Daten gefunden!");
                alert.showAndWait();
                return;
            case -4:
                // titles not unique
                alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Die Titelzeile enthält Elemente mit gleichen Namen.",
                        ButtonType.OK);
                alert.setHeaderText("Titel nicht einzigartig!");
                alert.showAndWait();
                return;
            case -5:
                // Selection column not found
                alert = new Alert(
                        Alert.AlertType.ERROR,
                        "In der Zeile "+excelSheet.getTitleRow()+" " +
                                "existiert keine Spalte mit dem Namen '" +
                                excelSheet.getSelectionColTitle() + "'.",
                        ButtonType.OK);
                alert.setHeaderText("Übernahmespalte nicht gefunden!");
                alert.showAndWait();
                return;
            case -6:
                // Cell evaluation error
                alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Einige Zellen konnten nicht evaluiert werden. Diese wurden mit 'ERROR' gefüllt. ",
                        ButtonType.OK);
                alert.setHeaderText("Evaluierungs Fehler!");

                TextArea textArea = new TextArea(
                        "Einige Zellen konnten nicht evaluiert werden. Diese wurden mit ERROR gefüllt. " +
                        "Genauere Informationen befinden sich im Log.\n" +
                        "Die von POI unterstützten Funktionen können hier nachgeschlagen werden: \n\n" +
                        "https://poi.apache.org/components/spreadsheet/eval-devguide.html");
                textArea.setEditable(false);
                textArea.setWrapText(true);
                GridPane gridPane = new GridPane();
                gridPane.setMaxWidth(Double.MAX_VALUE);
                gridPane.add(textArea, 0, 0);
                alert.getDialogPane().setContent(gridPane);
                alert.show();
                break;
        }

        stockDataCorrelationTable.getColumns().clear();
        stockDataCorrelationTable.getItems().clear();
        transactionCorrelationTable.getColumns().clear();
        transactionCorrelationTable.getItems().clear();
        importTabManagement.fillStockDataCorrelationTable(stockDataCorrelationTable, excelSheet);
        importTabManagement.fillTransactionCorrelationTable(transactionCorrelationTable, excelSheet);
        // refresh because otherwise the comboboxes are unreliable set
        stockDataCorrelationTable.refresh();
        transactionCorrelationTable.refresh();
    }

    @FXML
    private void importExcel() {
        importTabManagement.extractCorrelationData();
    }

    @FXML
    private void openLog() {

        Stage stage = new Stage();

        Scene scene = new Scene(this.logTextArea);
        scene.getStylesheets().add("style.css");
        stage.setScene(scene);

        stage.initOwner(pathField.getScene().getWindow());
        stage.initModality(Modality.NONE);
        stage.show();


        stage.setTitle("Log");
    }

    private boolean excelIsSelected() {
        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        if(excelSheet == null) {
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Wählen Sie eine Exceldatei aus der Liste aus.",
                    ButtonType.OK);
            alert.setHeaderText("Keine Excel ausgewählt!");
            alert.showAndWait();
            return false;
        }
        return true;
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

        sheetPreviewTable.getColumns().clear();
        sheetPreviewTable.getItems().clear();
        stockDataCorrelationTable.getColumns().clear();
        stockDataCorrelationTable.getItems().clear();
        stockDataCorrelationTable.setMinHeight(-1);
        transactionCorrelationTable.getColumns().clear();
        transactionCorrelationTable.getItems().clear();
        transactionCorrelationTable.setMinHeight(-1);
    }

    private boolean validPath() {
        pathField.getStyleClass().remove("bad-input");
        pathField.setTooltip(null);

        if(pathField.getText() == null || pathField.getText().isBlank()) {
            pathField.setTooltip(importTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            pathField.getStyleClass().add("bad-input");
            return false;
        } else if (!pathField.getText().matches("^.*\\.xlsx$")) {
            pathField.setTooltip(importTabManagement.createTooltip("Dieses Feld darf nur auf xlsx Dateien verweisen!"));
            pathField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }

//    private boolean validPassword() {
//        passwordField.getStyleClass().remove("bad-input");
//        passwordField.setTooltip(null);
//
//        if(passwordField.getText() == null || passwordField.getText().isBlank()) {
//            passwordField.setTooltip(importTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
//            passwordField.getStyleClass().add("bad-input");
//            return false;
//        }
//        return true;
//    }

    private boolean validTitleColNr() {
        titleRowNrField.getStyleClass().remove("bad-input");
        titleRowNrField.setTooltip(null);

        if(titleRowNrField.getText() == null || titleRowNrField.getText().isBlank()) {
            titleRowNrField.setTooltip(importTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            titleRowNrField.getStyleClass().add("bad-input");
            return false;
        } else if (!titleRowNrField.getText().matches("^[1-9][0-9]*$")) {
            titleRowNrField.setTooltip(importTabManagement.createTooltip(
                    "Dieses Feld darf nur eine Kombination der Zahlen 0-9 enthalten!"));
            titleRowNrField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }

    private boolean validSelectionColTitle() {
        selectionColTitleField.getStyleClass().remove("bad-input");
        selectionColTitleField.setTooltip(null);

        if(selectionColTitleField.getText() == null || selectionColTitleField.getText().isBlank()) {
            selectionColTitleField.setTooltip(importTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            selectionColTitleField.getStyleClass().add("bad-input");
            return false;
        }

        return true;
    }

    private boolean isValidInput() {
        // need all methods executed to highlight errors
        boolean valid = validPath();
//        valid &= validPassword();
        valid &= validTitleColNr();
        valid &= validSelectionColTitle();
        return valid;
    }
}
