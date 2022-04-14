package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management.VisualizationDataManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Controller
public class VisualizationTabController {
    @Autowired
    private VisualizationDataManager visualizationDataManager;

    @FXML
    private CheckBox normalizeCheckbox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button openNewWindowButton;

    @FXML
    private TableView<StockSelection> selectionTable;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Canvas canvas;
    @FXML
    private NumberAxis xAxis;

    private final List<StockSelection> selectedStocks = new ArrayList<>();

    @FXML
    public void initialize() {
        prepareTools();

        prepareSelectionTable();
        fillSelectionTable();

        prepareLineChart();
    }

    @FXML
    public void openNewWindow() {
        PrimaryTabManager.loadFxml(
                "gui/tabs/visualization/controller/visualizeTab.fxml",
                "Darstellung",
                openNewWindowButton,
                true, this);
    }

    private void prepareTools() {
        normalizeCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            loadData();
        });

        startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
            loadData(startDatePicker.getValue(), endDatePicker.getValue());
        });

        endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
            loadData(startDatePicker.getValue(), endDatePicker.getValue());
        });
    }

    private void prepareSelectionTable() {
        var isinCol = new TableColumn<StockSelection, String>("ISIN");
        var nameCol = new TableColumn<StockSelection, String>("Name");
        var isSelectedCol = new TableColumn<StockSelection, Boolean>("Selektion");

        isinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsin()));
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        isSelectedCol.setCellValueFactory(param -> param.getValue().isSelected());

        isinCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        isSelectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedCol));
        isSelectedCol.setCellValueFactory(row -> {
            var stockSelection = row.getValue();
            SimpleBooleanProperty sbp = stockSelection.isSelected();
            sbp.addListener((o, ov, nv) -> {
                if (nv && !ov) {
                    if (!selectedStocks.contains(stockSelection)) {
                        selectedStocks.add(stockSelection);
                        loadData();
                    }
                } else if (!nv && ov) {
                    if (selectedStocks.contains(stockSelection)) {
                        selectedStocks.remove(stockSelection);
                        loadData();
                    }
                }
            });

            return sbp;
        });

        isinCol.setEditable(false);
        nameCol.setEditable(false);
        isSelectedCol.setEditable(true);

        isinCol.setPrefWidth(100);
        nameCol.setPrefWidth(100);
        isSelectedCol.setPrefWidth(70);

        selectionTable.getColumns().add(isinCol);
        selectionTable.getColumns().add(nameCol);
        selectionTable.getColumns().add(isSelectedCol);
    }

    public void fillSelectionTable() {
        selectionTable.getItems().clear();

        var stocks = visualizationDataManager.getStocksWithCourseData();

        for (var stockSelection : stocks) {
            selectionTable.getItems().add(stockSelection);
        }
    }

    private void prepareLineChart() {
        lineChart.setAnimated(false);

        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        xAxis.setForceZeroInRange(false);

        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number timestamp) {
                return dateFormat.format(new Date(timestamp.longValue()));
            }

            @Override
            public Number fromString(String s) {
                try {
                    return dateFormat.parse(s).getTime();
                } catch (ParseException ignored) {

                }

                return 0.0;
            }
        });
    }

    private void loadData() {
        loadData(startDatePicker.getValue(), endDatePicker.getValue());
    }

    private void loadData(LocalDate startDate, LocalDate endDate) {
        resetChart();

        if (selectedStocks.size() == 0) return;

        var firstSelectedStock = (StockSelection) selectedStocks.get(0);

        for (var tableItem : selectedStocks) {
            if (!tableItem.isSelected().getValue()) continue;

            var data = visualizationDataManager.getHistoricPricesForIsin(tableItem.getIsin(), startDate, endDate);
            data.setName(tableItem.getName());

            if (normalizeCheckbox.isSelected() && !tableItem.getIsin().equals(firstSelectedStock.getIsin())) {
                data = visualizationDataManager.normalizeData(data, firstSelectedStock);
            }

            lineChart.getData().addAll(data);
        }
    }

    public void resetChart() {
        lineChart.getData().clear();
    }
}
