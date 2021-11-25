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

@Controller
public class StockTabController {

    @FXML private TableView<Stock> stockSelectionTable;
    @FXML private TableView<CustomRow> stockDataTableView;

    // column modification sub menu
    @FXML private GridPane columnSubmenuPane;
    @FXML private ChoiceBox<ColumnDatatype> columnDatatypeChoiceBox;
    @FXML private ComboBox<DbTableColumn> columnDeletionComboBox;
    @FXML private TextField columnNameField;

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

        updateCollumChoiceBox();
        columnDeletionComboBox.setValue(null);


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
        stockAndCourseTabManager.saveChangedRows(changedRows);
        stockAndCourseTabManager.saveStockListChanges(stockSelectionTable.getItems());
        scrapingElementsTabController.refresh();
        // TODO sucess alert
    }


    @FXML
    private void handleDeleteRowsButton() {
        // todo alert request confirm
        var selection = stockDataTableView.getSelectionModel().getSelectedItems();
        if(selection == null) return;
        var tmp = viewEverything;
        stockAndCourseTabManager.deleteRows(selection, false);
        reloadAllDataRows();
        if(tmp) handleShowAllButton();
        // todo success alert
    }

    @FXML
    private void handleDeleteAllInTableButton() {
        // todo alert request confirm
        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null || allRows == null || allRows.isEmpty()) return;
        var selectionRows = stockAndCourseTabManager.getRowsFromStockSelection(selected,allRows);
        stockAndCourseTabManager.deleteRows(selectionRows, true);
        reloadAllDataRows();
        // todo success alert
    }

    @FXML
    private void handleDeleteStockButton() {
        // todo alert request confirm
        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null) return;
        stockAndCourseTabManager.deleteStock(selected);
        scrapingElementsTabController.refresh();
        stockSelectionTable.getSelectionModel().selectFirst();
        handleResetButton();
        // todo success alert
    }

    @FXML
    private void handleNewStockButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/dbdata/controller/newStockPopup.fxml",
                "Wertpapier anlegen",
                stockSelectionTable,
                true, newStockPopupController);
    }

    @FXML
    private void handleColumnModificationButton() {
        showSubMenu(!columnSubmenuPane.isVisible());
    }


    private void onStockSelection(Stock stock) {
        lastViewed = stock;
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
    }

    private void reloadSelectionTable() {
        stockSelectionTable.getItems().clear();
        stockAndCourseTabManager.updateStockSelectionTable(stockSelectionTable);
    }

    private void redoSelection() {
        if(lastViewed != null) stockSelectionTable.getSelectionModel().select(lastViewed);
        else stockSelectionTable.getSelectionModel().selectFirst();
    }

    private void addRowChangeListeners() {
        changedRows.clear();
        for(CustomRow row : allRows) {
            row.isChangedProperty().addListener((o,ov,nv) -> changedRows.add(row));
        }
    }

    private void showSubMenu(boolean show) {
        if(show) columnSubmenuPane.setMaxHeight(50);
        else columnSubmenuPane.setMaxHeight(0);

        columnSubmenuPane.setVisible(show);
        columnSubmenuPane.setManaged(show);
    }

    private void updateCollumChoiceBox() {
        columnDeletionComboBox.getItems().clear();
        columnDeletionComboBox.getItems().add(null);
        columnDeletionComboBox.getItems().addAll(stockAndCourseTabManager.getStockColumns());
    }

    @FXML
    private void handleAddColumnButton() {
        // todo validation
        stockDataDbManager.addColumn(columnNameField.getText(), columnDatatypeChoiceBox.getValue());
        updateCollumChoiceBox();
        reloadAllDataRows();
    }

    @FXML
    private void handleRemoveColumnButton() {
        // todo validation
        // todo alert request confirm
        stockDataDbManager.removeColumn(columnDeletionComboBox.getSelectionModel().getSelectedItem().getName());
        reloadAllDataRows();
        updateCollumChoiceBox();
        // todo success alert
    }
}
