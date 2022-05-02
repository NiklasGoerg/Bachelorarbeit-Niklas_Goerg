package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management.VisualizationDataManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
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
import java.util.ArrayList;
import java.util.List;

@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisualizationCourseTabController extends VisualizationTabControllerTab {
    @FXML
    private TableView<StockSelection> selectionTable;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private Canvas canvas;
    @FXML
    private NumberAxis xAxis;

    @Autowired
    private VisualizationDataManager visualizationDataManager;

    private final List<StockSelection> selectedStocks = new ArrayList<>();

    @FXML
    public void initialize() {
        initializeUI();
    }

    @Override
    public void prepareCharts() {
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);

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

    @Override
    public void prepareSelectionTables() {
        var wknCol = new TableColumn<StockSelection, String>("WKN");
        var nameCol = new TableColumn<StockSelection, String>("Name");
        var isinCol = new TableColumn<StockSelection, String>("ISIN");
        var isSelectedCol = new TableColumn<StockSelection, Boolean>("Selektion");

        wknCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWkn()));
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        isinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsin()));
        isSelectedCol.setCellValueFactory(param -> param.getValue().isSelected());

        wknCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        isinCol.setCellFactory(TextFieldTableCell.forTableColumn());

        isSelectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedCol));
        isSelectedCol.setCellValueFactory(row -> {
            var stockSelection = row.getValue();
            SimpleBooleanProperty sbp = stockSelection.isSelected();
            sbp.addListener((o, ov, nv) -> {
                if (nv && !ov) {
                    if (!selectedStocks.contains(stockSelection)) {
                        selectedStocks.add(stockSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                } else if (!nv && ov) {
                    if (selectedStocks.contains(stockSelection)) {
                        selectedStocks.remove(stockSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                }
            });

            return sbp;
        });

        wknCol.setEditable(false);
        nameCol.setEditable(false);
        isinCol.setEditable(false);
        isSelectedCol.setEditable(true);

        wknCol.setPrefWidth(100);
        nameCol.setPrefWidth(100);
        isinCol.setPrefWidth(100);
        isSelectedCol.setPrefWidth(70);

        selectionTable.getColumns().add(wknCol);
        selectionTable.getColumns().add(isinCol);
        selectionTable.getColumns().add(nameCol);
        selectionTable.getColumns().add(isSelectedCol);
    }

    @Override
    public void fillSelectionTables() {
        selectionTable.getItems().clear();

        var stocks = visualizationDataManager.getStocksWithCourseData();

        for (var stockSelection : stocks) {
            selectionTable.getItems().add(stockSelection);
        }
    }

    public void loadData(LocalDate startDate, LocalDate endDate) {
        resetCharts();

        if (selectedStocks.size() == 0) return;

        var firstSelectedStock = (StockSelection) selectedStocks.get(0);

        for (var tableItem : selectedStocks) {
            if (!tableItem.isSelected().getValue()) continue;

            var data = visualizationDataManager.getHistoricPricesForIsin(tableItem.getIsin(), startDate, endDate);

            if(data == null || data.getData().size() == 0) return;

            data.setName(tableItem.getName());

            if (normalizeCheckbox.isSelected() && !tableItem.getIsin().equals(firstSelectedStock.getIsin())) {
                data = visualizationDataManager.normalizeData(data, firstSelectedStock);
            }

            lineChart.getData().add(data);
        }
    }

    @Override
    public void resetCharts() {
        lineChart.getData().clear();
    }

    @Override
    public void resetSelections() {
        selectedStocks.clear();
    }
}
