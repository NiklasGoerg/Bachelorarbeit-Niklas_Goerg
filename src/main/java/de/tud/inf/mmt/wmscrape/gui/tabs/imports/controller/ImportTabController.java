package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelSheet;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.management.ImportTabManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
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
    @FXML private Spinner<Integer> titleRowSpinner;
    @FXML private TextField selectionColTitleField;
    @FXML private TextField depotColTitleField;
    @FXML private TableView<ObservableList<String>> sheetPreviewTable;
    @FXML private TableView<ExcelCorrelation> stockDataCorrelationTable;
    @FXML private TableView<ExcelCorrelation> transactionCorrelationTable;
    @FXML private GridPane rightPanelBox;
    @FXML private SplitPane rootNode;

    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private NewExcelPopupController newExcelPopupController;
    @Autowired
    private ImportTabManager importTabManager;

    private ObservableList<ExcelSheet> excelSheetObservableList;
    private boolean inlineValidation = false;
    private static final BorderPane noSelectionReplacement = new BorderPane(new Label("Wählen Sie eine Excelkonfiguration aus oder erstellen Sie eine neue (unten links)"));

    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;

    private TextArea logTextArea = new TextArea();
    private SimpleStringProperty logText;

    @FXML
    private void initialize() {

        setRightPanelBoxVisibile(false);
        sheetPreviewTable.setPlaceholder(getPlaceholder());
        stockDataCorrelationTable.setPlaceholder(getPlaceholder());
        transactionCorrelationTable.setPlaceholder(getPlaceholder());

        excelSheetObservableList = importTabManager.initExcelSheetList(excelSheetList);
        excelSheetList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldSheet, newSheet) -> loadSpecificExcel(newSheet));

        pathField.textProperty().addListener((o,ov,nv) -> validPath());
        titleRowSpinner.valueProperty().addListener((o, ov, nv) -> validTitleColNr());
        selectionColTitleField.textProperty().addListener((o,ov,nv) -> emptyValidator(selectionColTitleField));
        depotColTitleField.textProperty().addListener((o,ov,nv) -> emptyValidator(depotColTitleField));

        logText = new SimpleStringProperty("");
        logTextArea = new TextArea();
        logTextArea.setPrefSize(350,400);
        logTextArea.textProperty().bind(logText);
        importTabManager.passLogText(logText);
        titleRowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1,1));
        excelSheetList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleNewExcelSheetButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/imports/controller/newExcelPopup.fxml",
                "Neue Konfiguration anlegen",
                excelSheetList,
                true, newExcelPopupController);
    }

    @FXML
    private void handleDeleteExcelSheetButton() {
        //clearFields();
        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        if(excelSheet == null) {
            createAlert("Keine Excel zum löschen ausgewählt!",
                    "Wählen Sie eine Konfiguration aus der Liste aus um diese zu löschen.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Einstellungen löschen?");
        alert.setContentText("Bitte bestätigen Sie, dass sie diese Konfiguration löschen möchten.");
        setAlertPosition(alert);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        importTabManager.deleteSpecificExcel(excelSheet);
        reloadExcelList();
        setRightPanelBoxVisibile(false);
        excelSheetList.getSelectionModel().selectFirst();
    }

    @FXML
    private void saveSpecificExcel() {
        if(excelIsNotSelected()) return;
        inlineValidation = true;
        if(!isValidInput()) return;

        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        excelSheet.setPath(pathField.getText());
        excelSheet.setPassword(passwordField.getText());
        excelSheet.setTitleRow(titleRowSpinner.getValue());
        excelSheet.setSelectionColTitle(selectionColTitleField.getText());
        excelSheet.setDepotColTitle(depotColTitleField.getText());

        importTabManager.saveExcel(excelSheet);

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Die Excelkonfiguration wurde gespeichert.",
                ButtonType.OK);
        alert.setHeaderText("Daten gespeichert!");
        setAlertPosition(alert);
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
        // remove changes if not saved
        loadSpecificExcel(excelSheetList.getSelectionModel().getSelectedItem());

        if(excelIsNotSelected()) return;
        inlineValidation = true;
        if(!isValidInput()) return;

        logText.set("");

        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();
        if(!importTabManager.sheetExists(excelSheet)) {
            createAlert("Datei nicht gefunden!",
                    "Unter dem angegebenen Pfad wurde keine gültige Datei gefunden.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return;
        }

        int result = importTabManager.fillExcelPreview(sheetPreviewTable, excelSheet);
        Alert alert;

        switch (result) {
            case -1:
                // wrong password
                createAlert("Falsches Passwort!",
                        "Das angegebene Passwort ist falsch. Speichern Sie bevor Sie die Vorschau laden.",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -2:
                // TitleRowError
                createAlert("Fehlerhafte Titelzeile!",
                        "Die Titelzeile liegt außerhalb der Begrenzung.",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -3:
                // no data in sheet
                createAlert("Keine Daten gefunden!",
                        "Die angegebene Datei enhält keine Daten.",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -4:
                // no data title row
                createAlert("Keine Daten gefunden!",
                        "In der angegebenen Titelzeile sind keine Daten.",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -5:
                // titles not unique
                createAlert("Titel nicht einzigartig!",
                        "Die Titelzeile enthält Elemente mit gleichen Namen. Mehr Informationen im Log",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -6:
                // Selection column not found
                createAlert("Übernahmespalte nicht gefunden!",
                        "In der Zeile "+(excelSheet.getTitleRow()+1)+" " +
                        "existiert keine Spalte mit dem Namen '" +
                        excelSheet.getSelectionColTitle() + "'.",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -7:
                // Selection column not found
                createAlert("Depotspalte nicht gefunden!",
                        "In der Zeile "+(excelSheet.getTitleRow()+1)+" " +
                                "existiert keine Spalte mit dem Namen '" +
                                excelSheet.getDepotColTitle() + "'.",
                        Alert.AlertType.ERROR, ButtonType.OK, true);
                return;
            case -8:
                // Cell evaluation error
                alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Einige Zellen konnten nicht evaluiert werden.",
                        ButtonType.OK);
                alert.setHeaderText("Fehler bei der Evaluierung!");

                TextArea textArea = new TextArea(
                        "Einige Zellen konnten nicht evaluiert werden.\n" +
                        "Genauere Informationen befinden sich im Log.\n" +
                        "Die von POI unterstützten Funktionen können hier nachgeschlagen werden: \n\n" +
                        "https://poi.apache.org/components/spreadsheet/eval-devguide.html");
                textArea.setEditable(false);
                textArea.setWrapText(true);
                GridPane gridPane = new GridPane();
                gridPane.setMaxWidth(Double.MAX_VALUE);
                gridPane.add(textArea, 0, 0);
                alert.getDialogPane().setContent(gridPane);
                setAlertPosition(alert);
                alert.show();
                break;
        }

        stockDataCorrelationTable.getColumns().clear();
        stockDataCorrelationTable.getItems().clear();
        transactionCorrelationTable.getColumns().clear();
        transactionCorrelationTable.getItems().clear();
        importTabManager.fillStockDataCorrelationTable(stockDataCorrelationTable, excelSheet);
        importTabManager.fillTransactionCorrelationTable(transactionCorrelationTable, excelSheet);
        // refresh because otherwise the comboboxes are unreliable set
        stockDataCorrelationTable.refresh();
        transactionCorrelationTable.refresh();
    }



    @FXML
    private void importExcel() {
        logText.set("");

        if (transactionCorrelationTable.getItems().isEmpty() || stockDataCorrelationTable.getItems().size() == 0) {
            createAlert("Vorschau nicht geladen!", "Die Vorschau muss vor dem Import geladen werden.",
                    Alert.AlertType.INFORMATION, ButtonType.OK, true);
            return;
        }
        int result = importTabManager.startDataExtraction();

        switch (result) {
            case 0 -> createAlert("Import abgeschlossen!",
                    "Alle Stammmdaten und Transaktionen wurden importiert.",
                    Alert.AlertType.INFORMATION, ButtonType.OK, true);
            case -1 -> createAlert("Import unvollständig!", "Nicht alle Zellen wurden " +
                            "importiert. Der Log enthält mehr Informationen.",
                    Alert.AlertType.WARNING, ButtonType.OK, true);
            case -2 -> createAlert("Vorschau nicht geladen!", "Die Vorschau muss vor dem Import geladen werden.",
                    Alert.AlertType.INFORMATION, ButtonType.OK, true);
            case -3 -> createAlert("Zuordnung unvollständig!",
                    "Es sind nicht alles notwendigen Zuordnungen gesetzt. Notwendig sind für " +
                            "Stammdaten:\n isin, wkn\nTransaktionen: wertpapier_isin, transaktions_datum, depot_name, transaktionstyp",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            case -4 -> createAlert("Fehler bei Sql-Statement erstellung.!",
                    "Bei der Erstellung der Sql-Statements kam es zu fehlern. Die Logs enthalten genauere Informationen.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            default -> createAlert("Fehler mit unbekannter Id!",
                    "Eine Fehlerbeschreibung zur Id: '" + result + "' existiert nicht",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
        }
    }

    @FXML
    private void openLog() {

        Stage stage = new Stage();
        Scene scene = logTextArea.getScene();

        if (scene == null) {
            scene = new Scene(this.logTextArea);
            scene.getStylesheets().add("style.css");
        } else {
            logTextArea.getScene().getWindow().hide();
        }

        stage.setScene(scene);

        stage.initOwner(pathField.getScene().getWindow());
        stage.initModality(Modality.NONE);
        stage.show();

        stage.setTitle("Log");
    }

    private boolean excelIsNotSelected() {
        ExcelSheet excelSheet = excelSheetList.getSelectionModel().getSelectedItem();

        if(excelSheet == null) {
            createAlert("Keine Excel ausgewählt!",
                    "Wählen Sie eine Excelkonfiguration aus der Liste aus oder erstellen Sie eine neue, bevor Sie Speichern.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return true;
        }
        return false;
    }

    public void selectLastExcel() {
        excelSheetList.getSelectionModel().selectLast();
    }

    public void reloadExcelList() {
        excelSheetObservableList.clear();
        excelSheetObservableList.addAll(importTabManager.getExcelSheets());
    }

    private void clearFields() {
        pathField.clear();
        passwordField.clear();
        titleRowSpinner.getValueFactory().setValue(1);
        selectionColTitleField.clear();
        sheetPreviewTable.getItems().clear();
        stockDataCorrelationTable.getItems().clear();
        transactionCorrelationTable.getItems().clear();
    }

    private void loadSpecificExcel(ExcelSheet excelSheet) {
        if (excelSheet == null) {
            return;
        }

        setRightPanelBoxVisibile(true);
        inlineValidation = false;
        logText.set("");

        pathField.setText(excelSheet.getPath());
        passwordField.setText(excelSheet.getPassword());
        titleRowSpinner.getValueFactory().setValue(excelSheet.getTitleRow());
        selectionColTitleField.setText(excelSheet.getSelectionColTitle());
        depotColTitleField.setText(excelSheet.getDepotColTitle());

        sheetPreviewTable.getColumns().clear();
        sheetPreviewTable.getItems().clear();
        stockDataCorrelationTable.getColumns().clear();
        stockDataCorrelationTable.getItems().clear();
        stockDataCorrelationTable.setMinHeight(-1);
        transactionCorrelationTable.getColumns().clear();
        transactionCorrelationTable.getItems().clear();
        transactionCorrelationTable.setMinHeight(-1);

        // here to remove eventually existing error styling
        isValidInput();
    }

    private boolean validPath() {
        return emptyValidator(pathField) && pathValidator(pathField);
    }

    private boolean validTitleColNr() {
        return titleRowSpinner.getValue() != null && titleRowSpinner.getValue() > 0;
    }

    private boolean isValidInput() {
        // need all methods executed to highlight errors
        boolean valid = validPath();
        valid &= validTitleColNr();
        valid &= emptyValidator(selectionColTitleField);
        return valid;
    }

    private boolean emptyValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && !input.getText().isBlank();
        decorateField(input, "Dieses Feld darf nicht leer sein!", isValid);
        return isValid;
    }

    private boolean numberValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && input.getText().matches("^[1-9][0-9]*$");
        decorateField(input, "Dieses Feld darf nur eine Kombination der Zahlen 0-9 enthalten!", isValid);
        return isValid;
    }

    private boolean pathValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && input.getText().matches("^.*\\.xlsx$");
        decorateField(input, "Dieses Feld darf nur auf xlsx Dateien verweisen!", isValid);
        return isValid;
    }

    private void decorateField(TextInputControl input, String tooltip, boolean isValid) {
        input.getStyleClass().remove("bad-input");
        input.setTooltip(null);

        if(!isValid) {
            if(inlineValidation) {
                input.setTooltip(importTabManager.createTooltip(tooltip));
                input.getStyleClass().add("bad-input");
            }
        }
    }

    private Label getPlaceholder() {
        return new Label("Keine Vorschau geladen.");
    }

    private void setRightPanelBoxVisibile(boolean visible) {
        if(!visible) {
            rootNode.getItems().remove(rightPanelBox);
            rootNode.getItems().add(noSelectionReplacement);
        } else {
            if(!rootNode.getItems().contains(rightPanelBox)) {
                rootNode.getItems().remove(noSelectionReplacement);
                rootNode.getItems().add(rightPanelBox);
                rootNode.setDividerPosition(0, 0);
            }
        }
    }

    private void createAlert(String title, String content, Alert.AlertType type, ButtonType buttonType, boolean wait) {
        Alert alert = new Alert(type, content, buttonType);
        alert.setHeaderText(title);
        setAlertPosition(alert);
        if(wait) alert.showAndWait();
    }

    private void setAlertPosition(Alert alert) {
        alert.setY(pathField.getScene().getWindow().getY() + (pathField.getScene().getWindow().getHeight() / 2) - 200);
        alert.setX(pathField.getScene().getWindow().getX() + (pathField.getScene().getWindow().getWidth() / 2) - 200);
    }
}
