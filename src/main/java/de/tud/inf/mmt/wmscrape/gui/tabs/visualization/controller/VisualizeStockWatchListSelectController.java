package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.WatchListSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management.VisualizationDataManager;
import de.tud.inf.mmt.wmscrape.helper.PropertiesHelper;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisualizeStockWatchListSelectController {
    @Autowired
    private VisualizationDataManager visualizationDataManager;

    @FXML
    private TableView<WatchListSelection> selectionTable;

    private StockSelection stock;
    private final Map<String, List<WatchListSelection>> watchListSelectionList = new HashMap<>();

    @FXML
    public void initialize() {
        setupColumns();

        var entries = visualizationDataManager.getWatchListData(stock);

        for(var entry : entries) {
            selectionTable.getItems().add(entry);
        }
    }

    public void setStock(StockSelection stock) {
        this.stock = stock;
    }

    public Map<String, List<WatchListSelection>> getSelection() {
        return watchListSelectionList;
    }

    public void clearSelection(String isin) {
        watchListSelectionList.get(isin).clear();
    }

    private void setupColumns() {
        var wknCol = new TableColumn<WatchListSelection, String>("WKN");
        var isinCol = new TableColumn<WatchListSelection, String>("ISIN");
        var dateCol = new TableColumn<WatchListSelection, Date>("Datum");
        var nameCol = new TableColumn<WatchListSelection, String>("Name");
        var priceCol = new TableColumn<WatchListSelection, Double>("Empf.-Preis");
        var amountCol = new TableColumn<WatchListSelection, Integer>("Stückzahl");
        var isSelectedCol = new TableColumn<WatchListSelection, Boolean>("Selektion");

        wknCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWkn()));
        isinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsin()));
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        dateCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getDate()));

        priceCol.setCellValueFactory(param -> new SimpleDoubleProperty(param.getValue().getPrice()).asObject());
        amountCol.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getAmount()).asObject());
        
        wknCol.setCellFactory(TextFieldTableCell.forTableColumn());
        isinCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        isSelectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedCol));
        isSelectedCol.setCellValueFactory(row -> {
            var stockSelection = row.getValue();
            SimpleBooleanProperty sbp = stockSelection.isSelectedProperty();
            sbp.addListener((o, ov, nv) -> {
                if (nv && !ov) {
                    if (!watchListSelectionList.containsKey(stockSelection.getIsin())) {
                        watchListSelectionList.put(stockSelection.getIsin(), new ArrayList<>());
                    }

                    if(!watchListSelectionList.get(stockSelection.getIsin()).contains(stockSelection)) {
                        watchListSelectionList.get(stockSelection.getIsin()).add(stockSelection);
                    }

                } else if (!nv && ov) {
                    watchListSelectionList.get(stockSelection.getIsin()).removeIf(watchListSelection -> watchListSelection.getIsin().equals(stockSelection.getIsin()));
                }
            });

            return sbp;
        });

        wknCol.setEditable(false);
        nameCol.setEditable(false);
        isinCol.setEditable(false);
        dateCol.setEditable(false);
        priceCol.setEditable(false);
        amountCol.setEditable(false);
        isSelectedCol.setEditable(true);

        wknCol.setPrefWidth(100);
        nameCol.setPrefWidth(100);
        isinCol.setPrefWidth(100);
        dateCol.setPrefWidth(80);
        priceCol.setPrefWidth(80);
        amountCol.setPrefWidth(80);
        isSelectedCol.setPrefWidth(70);

        selectionTable.getColumns().add(isSelectedCol);
        selectionTable.getColumns().add(wknCol);
        selectionTable.getColumns().add(isinCol);
        selectionTable.getColumns().add(dateCol);
        selectionTable.getColumns().add(nameCol);
        selectionTable.getColumns().add(priceCol);
        selectionTable.getColumns().add(amountCol);
    }

    @FXML
    public void closeModal(ActionEvent event) {
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();

        stage.hide();
    }
}
