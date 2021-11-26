package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.CourseDataManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.DataManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.ExchangeDataManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.StockDataManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class DataTabController {

    @Autowired
    private NewStockPopupController newStockPopupController;
    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    private CourseDataManager courseDataManager;
    @Autowired
    private StockDataManager stockDataManager;
    @Autowired
    private ExchangeDataManager exchangeDataManager;

    @FXML protected TableView<Stock> stockSelectionTable;
    @FXML protected TableView<CustomRow> customRowTableView;
    @FXML protected GridPane columnSubmenuPane;
    @FXML protected ChoiceBox<ColumnDatatype> columnDatatypeChoiceBox;
    @FXML protected ComboBox<DbTableColumn> columnDeletionComboBox;
    @FXML protected TextField newColumnNameField;
    @FXML private Tab stockTab;
    @FXML private Tab courseTab;
    @FXML private Tab exchangeTab;
    @FXML private TabPane sectionTabPane;
    @FXML private MenuItem createStockMenuItem;
    @FXML private MenuItem deleteStockMenuItem;
    @FXML private BorderPane stockSelectionPane;

    protected final ObservableList<CustomRow> changedRows = FXCollections.observableArrayList();
    protected ObservableList<CustomRow> allRows = FXCollections.observableArrayList();
    protected Stock lastViewed;
    protected boolean viewEverything = false;


    // initializing with stock data
    protected DataManager tabManager = stockDataManager;


    @FXML
    protected void initialize() {
        tabManager = stockDataManager;

        showSubMenu(false);
        columnDatatypeChoiceBox.getItems().setAll(ColumnDatatype.values());
        columnDatatypeChoiceBox.setValue(ColumnDatatype.TEXT);

        updateColumnComboBox();
        columnDeletionComboBox.setValue(null);

        newColumnNameField.textProperty().addListener((o,ov,nv) -> isValidName(nv));

        customRowTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tabManager.prepareStockSelectionTable(stockSelectionTable);
        reloadSelectionTable();
        stockSelectionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if(nv != null) onStockSelection(nv);
        });
        reloadAllDataRows();
        handleViewEverythingButton();
        registerTabChangeListener();
    }


    private void registerTabChangeListener() {
        sectionTabPane.getSelectionModel().selectedItemProperty().addListener((o,ov,nv) -> {

            if(nv != null) {
                if(nv.equals(stockTab)) {
                    tabManager = stockDataManager;
                    hideNonStockRelated(false);
                } else if (nv.equals(courseTab)) {
                    tabManager = courseDataManager;
                    hideNonStockRelated(false);
                } else if(nv.equals(exchangeTab)) {
                    tabManager = exchangeDataManager;
                    hideNonStockRelated(true);
                    handleViewEverythingButton();
                }

                updateColumnComboBox();
                handleResetButton();
            }
        });
    }

    private void hideNonStockRelated(boolean hide) {
        createStockMenuItem.setVisible(!hide);
        deleteStockMenuItem.setVisible(!hide);
        hideSelectionTable(hide);
    }

    private void hideSelectionTable(boolean hide) {
        if(hide) {
            stockSelectionPane.setMinWidth(0);
            stockSelectionPane.setMaxWidth(0);
        } else {
            stockSelectionPane.setMinWidth(165);
            stockSelectionPane.setMaxWidth(Double.MAX_VALUE);
        }
    }

    protected void reloadSelectionTable() {
        stockSelectionTable.getItems().clear();
        tabManager.updateStockSelectionTable(stockSelectionTable);
        redoSelection();
    }

    protected void updateColumnComboBox() {
        columnDeletionComboBox.getItems().clear();
        columnDeletionComboBox.getItems().add(null);
        columnDeletionComboBox.getItems().addAll(tabManager.getDbTableColumns());
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
    public void handleResetButton() {
        reloadSelectionTable();
        reloadAllDataRows();
    }

    @FXML
    private void handleDeleteStockButton() {

        Alert alert = confirmationAlert("Wertpapier löschen?", "Soll das ausgewählte Wertpapier gelöscht werden?.");
        if(wrongResponse(alert)) return;


        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null) return;
        tabManager.deleteStock(selected);
        scrapingElementsTabController.refresh();
        stockSelectionTable.getSelectionModel().selectFirst();
        handleResetButton();

        // delete returns void
        createAlert("Löschen erfolgreich!", "Das Wertpapier wurde gelöscht.",
                Alert.AlertType.INFORMATION, true, ButtonType.OK);
    }

    protected void showSubMenu(boolean show) {
        if(show) {
            newColumnNameField.clear();
            removeBadStyle();
            columnSubmenuPane.setMaxHeight(50);
        }
        else columnSubmenuPane.setMaxHeight(0);

        columnSubmenuPane.setVisible(show);
        columnSubmenuPane.setManaged(show);
    }

    protected void removeBadStyle() {
        newColumnNameField.getStyleClass().remove("bad-input");
        newColumnNameField.setTooltip(null);
    }

    @FXML
    private void handleViewEverythingButton() {
        customRowTableView.getColumns().clear();
        allRows = tabManager.updateDataTable(customRowTableView);
        customRowTableView.getItems().clear();
        customRowTableView.getItems().addAll(allRows);
        addRowChangeListeners();
        viewEverything = true;
    }

    protected void onStockSelection(Stock stock) {
        lastViewed = stock;
        viewEverything = false;
        customRowTableView.getItems().clear();
        var rows= tabManager.getStockRowsBySelection(stock, allRows);
        customRowTableView.getItems().addAll(rows);
        addRowChangeListeners();
    }

    protected void reloadAllDataRows() {
        customRowTableView.getColumns().clear();
        customRowTableView.getItems().clear();
        allRows = tabManager.updateDataTable(customRowTableView);
        redoSelection();
        changedRows.clear();
    }

    protected void redoSelection() {
        if(viewEverything) {
            handleViewEverythingButton();
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

    protected Alert createAlert(String title, String content, Alert.AlertType type, boolean wait, ButtonType... buttonType) {
        Alert alert = new Alert(type, content, buttonType);
        alert.setHeaderText(title);
        setAlertPosition(alert);
        if(wait) alert.showAndWait();
        return alert;
    }

    private void setAlertPosition(Alert alert) {
        var window = customRowTableView.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
    }

    protected boolean wrongResponse(Alert alert) {
        Optional<ButtonType> result = alert.showAndWait();
        return result.isEmpty() || !result.get().equals(ButtonType.YES);
    }

    protected Alert confirmationAlert(String title, String msg) {
        return createAlert(title, msg,
                Alert.AlertType.CONFIRMATION, false, ButtonType.NO, ButtonType.YES);
    }

    protected void messageOnSuccess(boolean success, String successTitle, String successMsg, String failTitle, String failMsg) {
        if (success) {
            createAlert(successTitle, successMsg, Alert.AlertType.INFORMATION, true, ButtonType.OK);
        } else {
            createAlert(failTitle, failMsg, Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    protected boolean isValidName(String text) {
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

    @FXML
    private void handleSaveButton() {
        var success = tabManager.saveChangedRows(changedRows);
        tabManager.saveStockListChanges(stockSelectionTable.getItems());
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

        var selection = customRowTableView.getSelectionModel().getSelectedItems();
        if(selection == null) return;

        boolean success = tabManager.deleteRows(selection, false);
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
        var selectionRows = tabManager.getStockRowsBySelection(selected,allRows);
        boolean success = tabManager.deleteRows(selectionRows, true);
        reloadAllDataRows();

        if (!success) {
            createAlert("Löschen nicht erfolgreich!", "Nicht alle Zeilen wurden gelöscht.",
                    Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    @FXML
    private void handleAddColumnButton() {


        if(!isValidName(newColumnNameField.getText())) return;

        int beforeCount = columnDeletionComboBox.getItems().size();

        if(newColumnNameField.getText().isBlank() || columnDatatypeChoiceBox.getValue() == null) return;

        tabManager.addColumn(newColumnNameField.getText(), columnDatatypeChoiceBox.getValue());

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
        boolean success = tabManager.removeColumn(colName);
        scrapingElementsTabController.refresh();
        reloadAllDataRows();
        updateColumnComboBox();

        messageOnSuccess(success, "Spalte gelöscht!",
                "Die Spalte mit dem Namen "+ colName +" wurde gelöscht.",
                "Löschen nicht erfolgreich!",
                "Die Spalte "+colName+" wurde nicht gelöscht.");
    }
}
