package de.tud.inf.mmt.wmscrape.gui.tabs.dbData.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.management.StockAndCourseTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class StockTabController {

    @FXML private TableView<Stock> stockSelectionTable;
    @FXML private TableView<CustomRow> stockDataTableView;

    @Autowired
    private StockAndCourseTabManager stockAndCourseTabManager;
    @Autowired
    ScrapingElementsTabController scrapingElementsTabController;

    private ObservableList<CustomRow> allRows = FXCollections.observableArrayList();
    private final ObservableList<CustomRow> changedRows = FXCollections.observableArrayList();
    private Stock lastViewed;
    private boolean viewEverything = false;

    @FXML
    private void initialize() {
        stockDataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        stockAndCourseTabManager.prepareStockSelectionTable(stockSelectionTable);
        reloadSelectionTable();
        stockSelectionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if(nv != null) onStockSelection(nv);
        });
        reloadAllDataRows();
    }

    @FXML
    private void showAll() {
        stockDataTableView.getColumns().clear();
        allRows = stockAndCourseTabManager.updateStockTable(stockDataTableView);
        stockDataTableView.getItems().clear();
        stockDataTableView.getItems().addAll(allRows);
        addRowChangeListeners();
        viewEverything = true;
    }

    @FXML
    public void resetAll() {
        reloadSelectionTable();
        reloadAllDataRows();
    }

    @FXML
    private void saveChanges() {
        stockAndCourseTabManager.saveChangedRows(changedRows);
        stockAndCourseTabManager.saveStockListChanges(stockSelectionTable.getItems());
        scrapingElementsTabController.refresh();
        // TODO sucess alert
    }


    @FXML
    private void deleteRows() {
        // todo alert request confirm
        var selection = stockDataTableView.getSelectionModel().getSelectedItems();
        if(selection == null) return;
        var tmp = viewEverything;
        stockAndCourseTabManager.deleteRows(selection, false);
        reloadAllDataRows();
        if(tmp) showAll();
        // todo success alert
    }

    @FXML
    private void deleteAllInTable() {
        // todo alert request confirm
        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null || allRows == null || allRows.isEmpty()) return;
        var selectionRows = stockAndCourseTabManager.getRowsFromStockSelection(selected,allRows);
        stockAndCourseTabManager.deleteRows(selectionRows, true);
        reloadAllDataRows();
        // todo success alert
    }

    @FXML
    private void deleteSelectedStock() {
        // todo alert request confirm
        var selected = stockSelectionTable.getSelectionModel().getSelectedItem();
        if( selected == null) return;
        stockAndCourseTabManager.deleteStock(selected);
        scrapingElementsTabController.refresh();
        stockSelectionTable.getSelectionModel().selectFirst();
        resetAll();
        // todo success alert
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
}
