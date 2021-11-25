package de.tud.inf.mmt.wmscrape.gui.tabs.dbData.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.management.StockAndCourseTabManager;
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

    private ObservableList<CustomRow> allRows = FXCollections.observableArrayList();
    private final ObservableList<CustomRow> changedRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        stockDataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        stockAndCourseTabManager.prepareStockSelectionTable(stockSelectionTable);
        reloadSelectionTable();
        stockSelectionTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> onStockSelection(nv));
        reloadAllDataRows();
    }

    @FXML
    private void showAll() {
        allRows = stockAndCourseTabManager.updateStockTable(stockDataTableView);
        stockDataTableView.getItems().clear();
        stockDataTableView.getItems().addAll(allRows);
        addRowChangeListeners();
    }

    private void onStockSelection(Stock stock) {
        stockDataTableView.getItems().clear();
        var rows= stockAndCourseTabManager.getRowsFromStockSelection(stock, allRows);
        stockDataTableView.getItems().addAll(rows);
        addRowChangeListeners();
    }

    @FXML
    private void reloadAllDataRows() {
        allRows = stockAndCourseTabManager.updateStockTable(stockDataTableView);
        redoSelection();
    }

    private void reloadSelectionTable() {
        stockAndCourseTabManager.updateStockSelectionTable(stockSelectionTable);
        redoSelection();
    }

    @FXML
    private void redoSelection() {
        var current = stockSelectionTable.getSelectionModel().getSelectedItem();
        if(current != null) onStockSelection(current);
        else stockSelectionTable.getSelectionModel().selectFirst();
    }

    private void addRowChangeListeners() {
        changedRows.clear();
        for(CustomRow row : allRows) {
            row.isChangedProperty().addListener((o,ov,nv) -> changedRows.add(row));
        }
    }

    @FXML
    private void saveChanges() {
        stockAndCourseTabManager.saveChangedRows(changedRows);
        // TODO sucess alert
    }


    @FXML
    private void deleteRows() {
        // todo alert request confirm
        var selection = stockDataTableView.getSelectionModel().getSelectedItems();
        if(selection == null) return;
        stockAndCourseTabManager.deleteRows(selection, false);
        reloadAllDataRows();
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
}
