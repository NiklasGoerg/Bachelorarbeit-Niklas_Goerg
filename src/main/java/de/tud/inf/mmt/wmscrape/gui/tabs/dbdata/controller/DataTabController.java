package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * todo due to the increased size of this controller it should be split up again into separate controller. the manager implementation can be used as is
 *
 * controller for the data tab. manages all pseudo tabs inside the data tab by changing the datasource and manager
 * responsible for filling the selection table, data table and executing operation based on the current manager
 *
 * as the size increased over time this controller should be split up and "real" tabs should be used
 */
@Controller
public class DataTabController {

    @Autowired private PrimaryTabManager primaryTabManager;
    @Autowired private ScrapingElementsTabController scrapingElementsTabController;
    @Autowired private CourseDataManager courseDataManager;
    @Autowired private StockDataManager stockDataManager;
    @Autowired private ExchangeDataManager exchangeDataManager;
    @Autowired private TransactionDataManager transactionDataManager;

    private final TableView<Stock> stockSelectionTable = new TableView<>();
    private final TableView<Depot> depotSelectionTable = new TableView<>();
    @FXML private BorderPane selectionPane;

    @FXML private TableView<CustomRow> customRowTableView;
    @FXML private SplitPane splitPane;
    @FXML private TextField newColumnNameField;
    @FXML private MenuItem createStockMenuItem;
    @FXML private MenuItem deleteStockMenuItem;
    @FXML private MenuItem addEmptyRowMenuItem;
    @FXML private MenuItem deleteDepotMenuItem;

    @FXML private TabPane sectionTabPane;
    @FXML private Tab stockTab;
    @FXML private Tab courseTab;
    @FXML private Tab exchangeTab;
    @FXML private Tab transactionTab;

    @FXML private GridPane columnSubmenuPane;
    @FXML private ChoiceBox<VisualDatatype> columnDatatypeChoiceBox;
    @FXML private ComboBox<DbTableColumn> columnDeletionComboBox;

    @FXML private GridPane stockCreateSubmenuPane;
    @FXML private TextField newIsinField;
    @FXML private TextField newWknField;
    @FXML private TextField newNameField;
    @FXML private TextField newTypeField;
    @FXML private TextField newSortOrderField;


    private final ObservableList<CustomRow> changedRows = FXCollections.observableArrayList();
    private ObservableList<CustomRow> allRows = FXCollections.observableArrayList();
    private Object lastViewed;
    private boolean viewEverything = false;

    private DataManager tabManager;

    /**
     * called when loading the fxml file
     *
     * sets the manager and repository to the stock data as default
     */
    @FXML
    private void initialize() {
        // initializing with stock data
        tabManager = stockDataManager;

        showColumnSubMenu(false);
        showStockSubMenu(false);
        columnDatatypeChoiceBox.getItems().setAll(VisualDatatype.values());
        columnDatatypeChoiceBox.setValue(VisualDatatype.Text);

        updateColumnComboBox();
        columnDeletionComboBox.setValue(null);

        newColumnNameField.textProperty().addListener((o,ov,nv) -> isValidName(nv));

        customRowTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tabManager.prepareStockSelectionTable(stockSelectionTable);
        tabManager.prepareDepotSelectionTable(depotSelectionTable);
        selectionPane.setCenter(stockSelectionTable);
        reloadSelectionTable();

        stockSelectionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if(nv != null) onSelection(nv);
        });

        depotSelectionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if(nv != null) onSelection(nv);
        });

        // validating the input at the stock creation submenu
        newIsinField.textProperty().addListener(x -> isValidIsin());
        newSortOrderField.textProperty().addListener(x -> isValidNumber());

        // when changing the columns inside the data table new listeners are added that register column sort changes
        customRowTableView.getColumns().addListener((ListChangeListener<TableColumn<CustomRow, ?>>) c -> tabManager.addColumnSortSync(customRowTableView, stockSelectionTable));

        reloadAllDataRows();
        handleViewEverythingButton();
        registerTabChangeListener();
        hideNonDepotRelated(true);
    }

    /**
     * reloads all data in the data-table without saving
     */
    @FXML
    public void handleResetButton() {
        reloadSelectionTable();
        reloadAllDataRows();
    }

    @FXML
    private void handleDeleteStockButton() {

        Alert alert = confirmationAlert("Wertpapier löschen?", "Soll das ausgewählte Wertpapier gelöscht werden?.");
        if(wrongResponse(alert)) return;


        var selected = stockSelectionTable.getSelectionModel().getSelectedItems();
        if( selected == null) return;
        tabManager.deleteStock(selected);
        scrapingElementsTabController.refresh();
        stockSelectionTable.getSelectionModel().selectFirst();
        handleResetButton();

        // delete returns void
        createAlert("Löschen erfolgreich!", "Das Wertpapier wurde gelöscht.",
                Alert.AlertType.INFORMATION, true, ButtonType.OK);
    }

    @FXML
    private void handleDeleteDepotButton() {

        Alert alert = confirmationAlert("Depot entfernen?", "Soll das ausgewählte Depot entfernt werden?.");
        if(wrongResponse(alert)) return;


        var selected = depotSelectionTable.getSelectionModel().getSelectedItems();
        if( selected == null) return;
        tabManager.deleteDepot(selected);
        depotSelectionTable.getSelectionModel().selectFirst();
        handleResetButton();

        // delete returns void
        createAlert("Löschen erfolgreich!", "Das Depot wurde gelöscht.",
                Alert.AlertType.INFORMATION, true, ButtonType.OK);
    }

    /**
     * shows all data rows without filtering by a selection
     */
    @FXML
    private void handleViewEverythingButton() {
        customRowTableView.getColumns().clear();
        customRowTableView.getItems().clear();
        allRows = tabManager.updateDataTable(customRowTableView);
        customRowTableView.getItems().addAll(allRows);
        customRowTableView.sort();
        addRowChangeListeners();
        viewEverything = true;
        stockSelectionTable.getSelectionModel().clearSelection();
        depotSelectionTable.getSelectionModel().clearSelection();
    }

    /**
     * starts the save process for modified rows
     */
    @FXML
    private void handleSaveButton() {
        var success = tabManager.saveChangedRows(changedRows);
        tabManager.saveStockListChanges(stockSelectionTable.getItems());
        scrapingElementsTabController.refresh();
        changedRows.clear();

        handleResetButton();
        messageOnSuccess(success, "Speichern erfolgreich!", "Alle Daten wurden gespeichert.",
                "Speichern nicht erfolgreich!",
                "Nicht alle Daten wurden gespeichert.");
    }

    /**
     * starts the deletion process for selected rows in the data-table
     */
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

    /**
     * starts the deletion process for all rows in the data-table
     * note: if something is selected only the filtered elements for that selection will be deleted
     */
    @FXML
    private void handleDeleteAllInTableButton() {

        Alert alert = confirmationAlert("Tabellendaten löschen?", "Sollen alle Zeilen in der angezeigten Tabelle gelöscht werden?.");
        if(wrongResponse(alert)) return;

        Object selected = ((TableView<?>) selectionPane.getCenter()).getSelectionModel().getSelectedItem();
        if(allRows == null || allRows.isEmpty()) return;

        boolean success;
        if(selected == null && viewEverything) {
            // delete *everything*
            success = tabManager.deleteRows(allRows, true);
        } else if (selected != null){

            ObservableList<CustomRow> selectionRows = null;

            // TODO refactor
            if(selected instanceof Stock) {
                selectionRows = tabManager.getRowsBySelection("isin", ((Stock) selected).getIsin(), allRows);
            } else if(selected instanceof Depot) {
                selectionRows = tabManager.getRowsBySelection("depot_name", ((Depot) selected).getName(), allRows);

            }

            success = tabManager.deleteRows(selectionRows, true);
        } else return;

        reloadAllDataRows();

        if (!success) {
            createAlert("Löschen nicht erfolgreich!", "Nicht alle Zeilen wurden gelöscht.",
                    Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    /**
     * shows the column modification sub-menu
     */
    @FXML
    private void handleColumnModificationButton() {
        showColumnSubMenu(!columnSubmenuPane.isVisible());
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
                "Die Spalte mit dem Namen "+ newColumnNameField.getText().toLowerCase()+" wurde hinzugefügt.",
                "Hinzufügen nicht erfolgreich!",
                "Die Spalte wurde nicht hinzugefügt.");
    }

    /**
     * shows the stock creation sub-menu
     */
    @FXML
    private void handleStockCreateSubMenuButton() {
        showStockSubMenu(!stockCreateSubmenuPane.isVisible());
    }

    @FXML
    private void handleNewStockButton() {

        if(!isValidStockCreationInput()) return;

        boolean success = stockDataManager.createStock(newIsinField.getText().trim(),
                                                        newWknField.getText().trim(),
                                                        newNameField.getText().trim(),
                                                        newTypeField.getText().trim(),
                                                        newSortOrderField.getText().trim());
        scrapingElementsTabController.refresh();

        if(success) {
            createAlert("Wertpapier angelegt!", "Ein neues Wertpapier wurde angelegt.",
                    Alert.AlertType.INFORMATION, true, ButtonType.OK);
            reloadSelectionTable();
        } else  {
            createAlert("Wertpapier nicht angelegt!", "Kein Wertpapier wurde angelegt. Wahrscheinlich existiert für " +
                            "die angegebene ISIN ein Wertpapier im System",
                    Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
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

    /**
     * adds a single row inside the current table with the current date if no row exists for the day
     */
    @FXML
    private void handleAddEmptyRow() {
        int before = customRowTableView.getItems().size();
        boolean noError = tabManager.addRowForSelection(
                ((TableView<?>)selectionPane.getCenter()).getSelectionModel().getSelectedItem());
        reloadAllDataRows();

        if(noError && customRowTableView.getItems().size() > before) {
            createAlert("Zeile hinzugefügt!", "Eine leere Zeile für das heutige Datum wurde hinzugefügt.",
                                Alert.AlertType.INFORMATION, true, ButtonType.OK);
        } else if (noError) {
            createAlert("Zeile nicht hinzugefügt!", "Keine Zeile wurde hinzugefügt. " +
                    "Wahrscheinlich existiert für das heutige Datum bereits eine Zeile.",
                    Alert.AlertType.WARNING, true, ButtonType.CLOSE);
        }  else if (viewEverything) {
            createAlert("Zeile nicht hinzugefügt!", "Es muss vorher eine Auswahl aus der linken " +
                            "Tabelle getroffen werden.",
                    Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        } else {
            createAlert("Zeile nicht hinzugefügt!", "Keine Zeile wurde hinzugefügt.",
                        Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    private void clearNewStockFields() {
        newIsinField.clear();
        newWknField.clear();
        newNameField.clear();
        newTypeField.clear();
        newSortOrderField.clear();
    }

    /**
     * handles the switching between the pseudo "tabs" by changing the manager and showing/hiding elements
     * it's not tly nice and should be split into separate tabs
     */
    private void registerTabChangeListener() {
        sectionTabPane.getSelectionModel().selectedItemProperty().addListener((o,ov,nv) -> {

            columnDatatypeChoiceBox.setDisable(false);

            if(nv != null) {
                if(nv.equals(stockTab)) {
                    tabManager = stockDataManager;
                    selectionPane.setCenter(stockSelectionTable);
                    hideNonStockRelated(false);
                    hideNonDepotRelated(true);
                    hideSelectionTable(false);
                    addEmptyRowMenuItem.setVisible(true);
                } else if (nv.equals(courseTab)) {
                    tabManager = courseDataManager;
                    selectionPane.setCenter(stockSelectionTable);
                    hideNonStockRelated(false);
                    hideNonDepotRelated(true);
                    hideSelectionTable(false);
                    addEmptyRowMenuItem.setVisible(true);
                } else if(nv.equals(exchangeTab)) {
                    tabManager = exchangeDataManager;
                    hideNonStockRelated(true);
                    hideNonDepotRelated(true);
                    hideSelectionTable(true);
                    addEmptyRowMenuItem.setVisible(true);
                    handleViewEverythingButton();

                    // only double values are allowed
                    columnDatatypeChoiceBox.setValue(VisualDatatype.Double);
                    columnDatatypeChoiceBox.setDisable(true);
                } else if(nv.equals(transactionTab)) {
                    tabManager = transactionDataManager;
                    selectionPane.setCenter(depotSelectionTable);
                    hideNonStockRelated(true);
                    hideNonDepotRelated(false);
                    hideSelectionTable(false);
                    addEmptyRowMenuItem.setVisible(false);
                    handleViewEverythingButton();
                }

                updateColumnComboBox();
                handleResetButton();
            }
        });
    }

    private void reloadSelectionTable() {
        TableView<?> table = (TableView<?>) selectionPane.getCenter();
        table.getItems().clear();

        // TODO refactor
        if (table == stockSelectionTable) {
            tabManager.updateStockSelectionTable(stockSelectionTable);
        } else if (table == depotSelectionTable) {
            tabManager.updateDepotSelectionTable(depotSelectionTable);
        }
        redoSelection();
    }

    /**
     * updates the column deletion combo box
     */
    private void updateColumnComboBox() {
        columnDeletionComboBox.getItems().clear();
        columnDeletionComboBox.getItems().add(null);
        columnDeletionComboBox.getItems().addAll(tabManager.getDbTableColumns());
    }

    /**
     * handles the selection change for the selection table
     * @param o a selected element from the selection table either
     * {@link de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock} or
     * {@link de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot}
     */
    private void onSelection(Object o) {
        lastViewed = o;
        viewEverything = false;
        customRowTableView.getItems().clear();
        ObservableList<CustomRow> rows = FXCollections.observableArrayList();

        // TODO refactor
        if(o instanceof Stock) {
            rows = tabManager.getRowsBySelection("isin", ((Stock) o).getIsin(), allRows);
        } else if(o instanceof Depot) {
            rows = tabManager.getRowsBySelection("depot_name",((Depot) o).getName(), allRows);
        }

        customRowTableView.getItems().addAll(rows);
        addRowChangeListeners();
    }

    private void reloadAllDataRows() {
        customRowTableView.getColumns().clear();
        customRowTableView.getItems().clear(); // also clears selection. useful when switching tabs
        allRows = tabManager.updateDataTable(customRowTableView);
        redoSelection();
        changedRows.clear();
    }

    /**
     * selects the last selected element useful after refreshing the data
     */
    private void redoSelection() {
        TableView<?> table = (TableView<?>) selectionPane.getCenter();

        if(viewEverything) {
            handleViewEverythingButton();
        } else if (lastViewed != null && (
                // TODO refactor
                (table == stockSelectionTable && lastViewed instanceof Stock && stockSelectionTable.getItems().contains((Stock) lastViewed)) ||
                (table == depotSelectionTable && lastViewed instanceof Depot && depotSelectionTable.getItems().contains((Depot) lastViewed)))) {
            onSelection(lastViewed);
        } else ((TableView<?>) selectionPane.getCenter()).getSelectionModel().selectFirst();
    }

    /**
     * adds listeners to all custom rows to detect changes
     */
    private void addRowChangeListeners() {
        changedRows.clear();
        for(CustomRow row : allRows) {
            row.isChangedProperty().addListener((o,ov,nv) -> changedRows.add(row));
        }
    }

    private Alert createAlert(String title, String content, Alert.AlertType type, boolean wait, ButtonType... buttonType) {
        Alert alert = new Alert(type, content, buttonType);
        alert.setHeaderText(title);
        PrimaryTabManager.setAlertPosition(alert , customRowTableView);
        if(wait) alert.showAndWait();
        return alert;
    }

    private boolean wrongResponse(Alert alert) {
        Optional<ButtonType> result = alert.showAndWait();
        return result.isEmpty() || !result.get().equals(ButtonType.YES);
    }

    private Alert confirmationAlert(String title, String msg) {
        return createAlert(title, msg,
                Alert.AlertType.CONFIRMATION, false, ButtonType.NO, ButtonType.YES);
    }

    private void messageOnSuccess(boolean success, String successTitle, String successMsg, String failTitle, String failMsg) {
        if (success) {
            createAlert(successTitle, successMsg, Alert.AlertType.INFORMATION, true, ButtonType.OK);
        } else {
            createAlert(failTitle, failMsg, Alert.AlertType.ERROR, true, ButtonType.CLOSE);
        }
    }

    private void hideNonStockRelated(boolean hide) {
        createStockMenuItem.setVisible(!hide);
        deleteStockMenuItem.setVisible(!hide);
        showStockSubMenu(false);
    }

    private void hideNonDepotRelated(boolean hide) {
        deleteDepotMenuItem.setVisible(!hide);
    }

    /**
     * hides the selection table where no selection is needed (e.g. the exchange price section)
     */
    private void hideSelectionTable(boolean hide) {
        if(hide) {
            selectionPane.setMaxWidth(0);
        } else {
            selectionPane.setMaxWidth(Double.MAX_VALUE);
            splitPane.setDividerPosition(0,0.2);
        }
    }

    private boolean isValidName(String text) {
        removeBadStyle();

        if(text == null || text.isBlank()) {
            return badTooltip("Dieses Feld darf nicht leer sein!");
        } else if (text.length()>=64) {
            return badTooltip("Die maximale Länge eines Spaltennamens ist 64 Zeichen.");
        } else if (!text.matches("^[a-zA-Z0-9üä][a-zA-Z0-9_äöüß]*$")) {
            return badTooltip("Der Name enthält unzulässige Symbole. Nur a-z,0-9,ä,ö,ü,ß,_, sind erlaubt.");
        }

        return true;
    }

    /**
     * removes the css highlighting
     */
    private void removeBadStyle() {
        newColumnNameField.getStyleClass().remove("bad-input");
        newColumnNameField.setTooltip(null);
        newIsinField.getStyleClass().remove("bad-input");
        newIsinField.setTooltip(null);
        newSortOrderField.getStyleClass().remove("bad-input");
        newSortOrderField.setTooltip(null);
    }

    /**
     * binds the error tooltip
     * @param message the tooltip message
     */
    @SuppressWarnings("SameReturnValue")
    private boolean badTooltip(String message) {
        newColumnNameField.setTooltip(PrimaryTabManager.createTooltip(message));
        newColumnNameField.getStyleClass().add("bad-input");
        return false;
    }

    /**
     * opens the column modification sub menu
     *
     * @param show if true show the submenu
     */
    private void showColumnSubMenu(boolean show) {
        if(show) {
            showStockSubMenu(false);
            newColumnNameField.clear();
            removeBadStyle();
            columnSubmenuPane.setMaxHeight(50);
        }
        else columnSubmenuPane.setMaxHeight(0);

        columnSubmenuPane.setVisible(show);
        columnSubmenuPane.setManaged(show);
    }

    private void showStockSubMenu(boolean show) {
        if(show) {
            showColumnSubMenu(false);
            clearNewStockFields();
            removeBadStyle();
            stockCreateSubmenuPane.setMaxHeight(50);
        }
        else stockCreateSubmenuPane.setMaxHeight(0);

        stockCreateSubmenuPane.setVisible(show);
        stockCreateSubmenuPane.setManaged(show);
    }

    /**
     * validates that the input is sufficient to create a stock
     *
     * @return true if valid
     */
    private boolean isValidStockCreationInput() {
        boolean valid = isValidIsin();
        valid &= isValidNumber();
        return valid;
    }

    private boolean isValidIsin() {
        newIsinField.getStyleClass().remove("bad-input");
        newIsinField.setTooltip(null);

        String text = newIsinField.getText();

        if(text == null || text.isBlank()) {
            addTooltip(newIsinField,"Dieses Feld darf nicht leer sein!");
            return false;
        } else if (text.length()>=50) {
            addTooltip(newIsinField,"Die maximale Länge der ISIN beträgt 50 Zeichen.");
            return false;
        }

        return true;
    }

    /**
     * validates the "r_par" value
     *
     * @return true if its a numerical value
     */
    private boolean isValidNumber() {
        newSortOrderField.getStyleClass().remove("bad-input");
        newSortOrderField.setTooltip(null);

        String text = newSortOrderField.getText();

        if(text == null || text.isBlank()) {
            addTooltip(newSortOrderField,"Dieses Feld darf nicht leer sein!" );
            return false;
        } else if (!text.trim().matches("^[+-]?[0-9]+$")) {
            addTooltip(newSortOrderField,"Es muss eine gültige Ganzzahl angegeben werden.");
            return false;
        }
        return true;
    }

    private void addTooltip(Control control, String text) {
        control.setTooltip(PrimaryTabManager.createTooltip(text));
        control.getStyleClass().add("bad-input");
    }
}
