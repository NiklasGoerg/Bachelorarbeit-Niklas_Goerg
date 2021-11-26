package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.StockAndCourseTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class StockTabController {

    @FXML private TableView<Stock> stockSelectionTable;
    @FXML private TableView<CustomRow> stockDataTableView;

    // column modification sub menu
    @FXML private GridPane columnSubmenuPane;
    @FXML private ChoiceBox<ColumnDatatype> columnDatatypeChoiceBox;
    @FXML private ComboBox<DbTableColumn> columnDeletionComboBox;
    @FXML private TextField newColumnNameField;

    @Autowired
    private StockAndCourseTabManager stockAndCourseTabManager;
    @Autowired
    ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    NewStockPopupController newStockPopupController;
    @Autowired
    PrimaryTabManagement primaryTabManagement;
    @Autowired
    StockDataDbManager stockDataDbManager;


    private ObservableList<CustomRow> allRows = FXCollections.observableArrayList();
    private final ObservableList<CustomRow> changedRows = FXCollections.observableArrayList();
    private Stock lastViewed;
    private boolean viewEverything = false;

    @FXML
    private void initialize() {
        showSubMenu(false);
        columnDatatypeChoiceBox.getItems().setAll(ColumnDatatype.values());
        columnDatatypeChoiceBox.setValue(ColumnDatatype.TEXT);

        updateColumnComboBox();
        columnDeletionComboBox.setValue(null);

        newColumnNameField.textProperty().addListener((o,ov,nv) -> isValidName(nv));

        stockDataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        stockAndCourseTabManager.prepareStockSelectionTable(stockSelectionTable);
        reloadSelectionTable();
        stockSelectionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if(nv != null) onStockSelection(nv);
        });
        reloadAllDataRows();
    }

    @FXML
    private void handleShowAllButton() {
        stockDataTableView.getColumns().clear();
        allRows = stockAndCourseTabManager.updateStockTable(stockDataTableView);
        stockDataTableView.getItems().clear();
        stockDataTableView.getItems().addAll(allRows);
        addRowChangeListeners();
        viewEverything = true;
    }

    @FXML
    public void handleResetButton() {
        reloadSelectionTable();
        reloadAllDataRows();
    }

    @FXML
    private void handleSaveButton() {
        var success = stockAndCourseTabManager.saveChangedRows(changedRows);
        stockAndCourseTabManager.saveStockListChanges(stockSelectionTable.getItems());
        scrapingElementsTabController.refresh();
        changedRows.clear();

        messageOnSuccess(success, "Speichern erfolgreich!", "Alle Daten wurden gespeichert.",
                "Speichern nicht erfolgreich!",
                "Nicht alle Daten wurden gespeichert.");
    }

    @FXML
    private void handleDeleteRowsButton() {

        Alert alert = confirmationAlert("Zeilen löschen?", "Sollen die ausgewählten Zeilen gelöscht werden?.");
        if(wrongResponse(alert)) return;

        var selection = stockDataTableView.getSelectionModel().getSelectedItems();
        if(selection == null) return;

        boolean success = stockAndCourseTabManager.deleteRows(selection, false);
        reloadAllDataRows();

        messageOnSuccess(success, "Löschen erfolgreich!", "Die markierte Daten wurden gelöscht.",
                "Löschen nicht erfolgreich!",
                "Nicht alle Zeilen wurden gelöscht.");

    }

    @FXML
    private void handleDeleteAllInTableButton() {

        Alert alert = confirmationAlert("Tabellendaten löschen?", "Sollen alle Zeilen in der Tabelle gelöscht werden?.");
        if(wrongResponse(alert)) return;

        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null || allRows == null || allRows.isEmpty()) return;
        var selectionRows = stockAndCourseTabManager.getRowsFromStockSelection(selected,allRows);
        boolean success = stockAndCourseTabManager.deleteRows(selectionRows, true);
        reloadAllDataRows();

        if (!success) {
            createAlert("Löschen nicht erfolgreich!", "Nicht alle Zeilen wurden gelöscht.",
                    Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    @FXML
    private void handleDeleteStockButton() {

        Alert alert = confirmationAlert("Wertpapier löschen?", "Soll das ausgewählte Wertpapier gelöscht werden?.");
        if(wrongResponse(alert)) return;


        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null) return;
        stockAndCourseTabManager.deleteStock(selected);
        scrapingElementsTabController.refresh();
        stockSelectionTable.getSelectionModel().selectFirst();
        handleResetButton();

        // delete returns void
        createAlert("Löschen erfolgreich!", "Das Wertpapier wurde gelöscht.",
                Alert.AlertType.INFORMATION, true, ButtonType.OK);
    }

    @FXML
    private void handleNewStockButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/dbdata/controller/newStockPopup.fxml",
                "Wertpapier anlegen",
                stockSelectionTable,
                true, newStockPopupController);
        reloadSelectionTable();
    }

    @FXML
    private void handleColumnModificationButton() {
        showSubMenu(!columnSubmenuPane.isVisible());
    }

    @FXML
    private void handleAddColumnButton() {


        if(!isValidName(newColumnNameField.getText())) return;

        int beforeCount = columnDeletionComboBox.getItems().size();

        if(newColumnNameField.getText().isBlank() || columnDatatypeChoiceBox.getValue() == null) return;
        stockDataDbManager.addColumn(newColumnNameField.getText(), columnDatatypeChoiceBox.getValue());
        scrapingElementsTabController.refresh();
        updateColumnComboBox();
        reloadAllDataRows();

        int afterCount = columnDeletionComboBox.getItems().size();

        messageOnSuccess(afterCount>beforeCount, "Spalte hinzugefügt!",
                "Die Spalte mit dem Namen "+ newColumnNameField.getText()+" wurde hinzugefügt.",
                "Hinzufügen nicht erfolgreich!",
                "Die Spalte wurde nicht hinzugefügt.");
    }

    @FXML
    private void handleRemoveColumnButton() {
        if(columnDeletionComboBox.getSelectionModel().getSelectedItem() == null) return;

        Alert alert = confirmationAlert("Spalte löschen?", "Soll die ausgewählte Spalte gelöscht werden?.");
        if(wrongResponse(alert)) return;

        String colName = columnDeletionComboBox.getSelectionModel().getSelectedItem().getName();
        boolean success = stockDataDbManager.removeColumn(colName);
        scrapingElementsTabController.refresh();
        reloadAllDataRows();
        updateColumnComboBox();

        messageOnSuccess(success, "Spalte gelöscht!",
                "Die Spalte mit dem Namen "+ colName +" wurde gelöscht.",
                "Löschen nicht erfolgreich!",
                "Die Spalte "+colName+" wurde nicht gelöscht.");
    }


    private void onStockSelection(Stock stock) {
        lastViewed = stock;
        viewEverything = false;
        stockDataTableView.getItems().clear();
        var rows= stockAndCourseTabManager.getRowsFromStockSelection(stock, allRows);
        stockDataTableView.getItems().addAll(rows);
        addRowChangeListeners();
    }

    private void reloadAllDataRows() {
        stockDataTableView.getColumns().clear();
        stockDataTableView.getItems().clear();
        allRows = stockAndCourseTabManager.updateStockTable(stockDataTableView);
        redoSelection();
        changedRows.clear();
    }

    private void reloadSelectionTable() {
        stockSelectionTable.getItems().clear();
        stockAndCourseTabManager.updateStockSelectionTable(stockSelectionTable);
        redoSelection();
    }

    private void redoSelection() {
        if(viewEverything) {
            handleShowAllButton();
        } else if (lastViewed != null && stockSelectionTable.getItems().contains(lastViewed)) {
            onStockSelection(lastViewed);
        } else stockSelectionTable.getSelectionModel().selectFirst();
    }

    private void addRowChangeListeners() {
        changedRows.clear();
        for(CustomRow row : allRows) {
            row.isChangedProperty().addListener((o,ov,nv) -> changedRows.add(row));
        }
    }

    private void showSubMenu(boolean show) {
        if(show) {
            newColumnNameField.clear();
            removeBadStyle();
            columnSubmenuPane.setMaxHeight(50);
        }
        else columnSubmenuPane.setMaxHeight(0);

        columnSubmenuPane.setVisible(show);
        columnSubmenuPane.setManaged(show);
    }

    private void updateColumnComboBox() {
        columnDeletionComboBox.getItems().clear();
        columnDeletionComboBox.getItems().add(null);
        columnDeletionComboBox.getItems().addAll(stockAndCourseTabManager.getStockColumns());
    }


    private Alert createAlert(String title, String content, Alert.AlertType type, boolean wait, ButtonType... buttonType) {
        Alert alert = new Alert(type, content, buttonType);
        alert.setHeaderText(title);
        setAlertPosition(alert);
        if(wait) alert.showAndWait();
        return alert;
    }

    private void setAlertPosition(Alert alert) {
        var window = stockDataTableView.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
    }

    private boolean wrongResponse(Alert alert) {
        Optional<ButtonType> result = alert.showAndWait();
        return result.isEmpty() || !result.get().equals(ButtonType.YES);
    }

    private Alert confirmationAlert(String title, String msg) {
        return createAlert(title, msg,
                Alert.AlertType.CONFIRMATION, false, ButtonType.NO, ButtonType.YES);
    }

    public void messageOnSuccess(boolean success, String successTitle,  String successMsg, String failTitle, String failMsg) {
        if (success) {
            createAlert(successTitle, successMsg, Alert.AlertType.INFORMATION, true, ButtonType.OK);
        } else {
            createAlert(failTitle, failMsg, Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    private boolean isValidName(String text) {
        removeBadStyle();

        if(text == null || text.isBlank()) {
            return badTooltip("Dieses Feld darf nicht leer sein!");
        } else if (text.length()>=64) {
            return badTooltip("Die maximale Länge eines Spaltennamens ist 64 Zeichen.");
        } else if (!text.matches("^[a-zA-Z0-9üä][a-zA-Z0-9_\\-+äöüß]*$")) {
            return badTooltip("Der Name enthält unzulässige Symbole. Nur a-z,0-9,ä,ö,ü,ß,-,+,_, sind erlaubt.");
        }

        return true;
    }

    private boolean badTooltip(String message) {
        newColumnNameField.setTooltip(PrimaryTabManagement.createTooltip(message));
        newColumnNameField.getStyleClass().add("bad-input");
        return false;
    }

    private void removeBadStyle() {
        newColumnNameField.getStyleClass().remove("bad-input");
        newColumnNameField.setTooltip(null);
    }
}
