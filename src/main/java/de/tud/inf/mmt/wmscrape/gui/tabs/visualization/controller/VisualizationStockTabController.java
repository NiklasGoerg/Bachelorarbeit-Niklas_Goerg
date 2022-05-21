package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ExtractedParameter;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ParameterSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management.VisualizationDataManager;
import de.tud.inf.mmt.wmscrape.helper.PropertiesHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisualizationStockTabController extends VisualizationTabControllerTab {
    @FXML
    private TableView<StockSelection> stockSelectionTable;
    @FXML
    private TableView<ParameterSelection> parameterSelectionTable;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private NumberAxis lineXAxis;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private Canvas canvas;

    @Autowired
    private VisualizationDataManager visualizationDataManager;

    private final List<StockSelection> selectedStocks = new ArrayList<>();

    private final List<StockSelection> selectedTransactions = new ArrayList<>();
    private final List<StockSelection> selectedWatchList = new ArrayList<>();

    private final List<ParameterSelection> selectedParameters = new ArrayList<>();

    private boolean alarmIsOpen = false;

    @FXML
    public void initialize() {
        initializeUI();
    }

    @Override
    public void prepareCharts() {
        lineChart.setVisible(true);
        barChart.setVisible(false);

        lineChart.setAnimated(false);
        barChart.setAnimated(false);

        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        lineXAxis.setForceZeroInRange(false);

        lineXAxis.setTickLabelFormatter(new StringConverter<>() {
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
        setupStockSelectionTable();
        setupParameterSelectionTable();
    }

    private void setupStockSelectionTable() {
        var wknCol = new TableColumn<StockSelection, String>("WKN");
        var isinCol = new TableColumn<StockSelection, String>("ISIN");
        var nameCol = new TableColumn<StockSelection, String>("Name");
        var isSelectedCol = new TableColumn<StockSelection, Boolean>("Selektion");
        var isSelectedTransactionCol = new TableColumn<StockSelection, Boolean>("Transaktionen");
        var isSelectedWatchListCol = new TableColumn<StockSelection, Boolean>("Watch-Liste");

        wknCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWkn()));
        isinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsin()));
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        isSelectedCol.setCellValueFactory(param -> param.getValue().isSelected());

        isSelectedTransactionCol.setCellValueFactory(param -> param.getValue().isTransactionSelectedProperty());
        isSelectedWatchListCol.setCellValueFactory(param -> param.getValue().isWatchListSelectedProperty());

        wknCol.setCellFactory(TextFieldTableCell.forTableColumn());
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

        isSelectedTransactionCol.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedCol));
        isSelectedTransactionCol.setCellValueFactory(row -> {
            var stockSelection = row.getValue();
            SimpleBooleanProperty sbp = stockSelection.isTransactionSelectedProperty();
            sbp.addListener((o, ov, nv) -> {
                if(PropertiesHelper.getProperty("TransaktionAnzahlSpaltenName") == null) {
                    if(nv && !alarmIsOpen) {
                        sbp.set(false);
                        createAlert("Zuordnung zur Anzahl-Spalte der Transaktionstabelle fehlt.");
                    }

                    return;
                }

                if (nv && !ov) {
                    if (!selectedTransactions.contains(stockSelection)) {
                        selectedTransactions.add(stockSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                } else if (!nv && ov) {
                    if (selectedTransactions.contains(stockSelection)) {
                        selectedTransactions.remove(stockSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                }
            });

            return sbp;
        });

        isSelectedWatchListCol.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedCol));
        isSelectedWatchListCol.setCellValueFactory(row -> {
            var stockSelection = row.getValue();
            SimpleBooleanProperty sbp = stockSelection.isWatchListSelectedProperty();
            sbp.addListener((o, ov, nv) -> {
                if(PropertiesHelper.getProperty("WatchListeAnzahlSpaltenName") == null) {
                    if(nv && !alarmIsOpen) {
                        sbp.set(false);
                        createAlert("Zuordnung zur Anzahl-Spalte der Watch-List-Tabelle fehlt.");
                    }

                    return;
                }

                if (nv && !ov) {
                    if (!selectedWatchList.contains(stockSelection)) {
                        selectedWatchList.add(stockSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                } else if (!nv && ov) {
                    if (selectedWatchList.contains(stockSelection)) {
                        selectedWatchList.remove(stockSelection);
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
        isSelectedTransactionCol.setEditable(true);
        isSelectedWatchListCol.setEditable(true);

        wknCol.setPrefWidth(100);
        nameCol.setPrefWidth(100);
        isinCol.setPrefWidth(100);
        isSelectedCol.setPrefWidth(70);
        isSelectedTransactionCol.setPrefWidth(70);
        isSelectedWatchListCol.setPrefWidth(70);

        stockSelectionTable.getColumns().add(wknCol);
        stockSelectionTable.getColumns().add(isinCol);
        stockSelectionTable.getColumns().add(nameCol);
        stockSelectionTable.getColumns().add(isSelectedCol);
        stockSelectionTable.getColumns().add(isSelectedTransactionCol);
        stockSelectionTable.getColumns().add(isSelectedWatchListCol);
    }

    private void setupParameterSelectionTable() {
        var parameterCol = new TableColumn<ParameterSelection, String>("Parameter");
        var isSelectedCol = new TableColumn<ParameterSelection, Boolean>("Selektion");

        parameterCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getParameter()));
        isSelectedCol.setCellValueFactory(param -> param.getValue().isSelected());

        parameterCol.setCellFactory(TextFieldTableCell.forTableColumn());

        isSelectedCol.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedCol));
        isSelectedCol.setCellValueFactory(row -> {
            var parameterSelection = row.getValue();
            SimpleBooleanProperty sbp = parameterSelection.isSelected();
            sbp.addListener((o, ov, nv) -> {
                if (nv && !ov) {
                    if (!selectedParameters.contains(parameterSelection)) {
                        selectedParameters.add(parameterSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                } else if (!nv && ov) {
                    if (selectedParameters.contains(parameterSelection)) {
                        selectedParameters.remove(parameterSelection);
                        loadData(startDatePicker.getValue(), endDatePicker.getValue());
                    }
                }
            });

            return sbp;
        });

        parameterCol.setEditable(false);
        isSelectedCol.setEditable(true);

        parameterCol.setPrefWidth(100);
        isSelectedCol.setPrefWidth(70);

        parameterSelectionTable.getColumns().add(parameterCol);
        parameterSelectionTable.getColumns().add(isSelectedCol);
    }

    @Override
    public void fillSelectionTables() {
        fillStockSelectionTable();
        fillParameterSelectionTable();
    }

    private void fillStockSelectionTable() {
        var stocks = visualizationDataManager.getStocksWithParameterData();

        for (var stockSelection : stocks) {
            if(stockSelectionTable.getItems().stream().noneMatch(s -> s.getIsin().equals(stockSelection.getIsin()))) {
                stockSelectionTable.getItems().add(stockSelection);
            }
        }
    }

    private void fillParameterSelectionTable() {
        var parameters = visualizationDataManager.getParameters();

        for (var parameterSelection : parameters) {
            if(parameterSelectionTable.getItems().stream().noneMatch(s -> s.getParameter().equals(parameterSelection.getParameter()))) {
                parameterSelectionTable.getItems().add(parameterSelection);
            }
        }
    }

    @Override
    public void loadData(LocalDate startDate, LocalDate endDate) {
        resetCharts();

        if (selectedStocks.size() == 0 || selectedParameters.size() == 0) return;

        Map<String, List<ObservableList<ExtractedParameter>>> allStocksData = new LinkedHashMap<>(selectedStocks.size());

        for (var tableItem : selectedStocks) {
            for(var parameter : selectedParameters) {
                if (!tableItem.isSelected().getValue()) continue;

                var data = visualizationDataManager.getParameterDataForIsin(tableItem.getIsin(), tableItem.getName(), parameter, startDate, endDate);

                if (data == null || data.size() == 0) continue;

                if(!allStocksData.containsKey(tableItem.getIsin())) {
                    allStocksData.put(tableItem.getIsin(), new ArrayList<>(selectedParameters.size()));
                }

                allStocksData.get(tableItem.getIsin()).add(data);
            }
        }

        if(selectedStocks.size() > 1 || selectedTransactions.size() > 1 || selectedWatchList.size() > 1) {
            showBarChart();

            var stockNames = new ArrayList<String>();

            for(var stock : allStocksData.keySet()) {
                var barChartData = visualizationDataManager.getBarChartParameterData(allStocksData.get(stock));

                addDataToBarChart(barChartData, stockNames);
            }

            if(selectedTransactions.size() > 0 || selectedWatchList.size() > 0) {
                var barChartData = visualizationDataManager.getBarChartDepotParameterData(
                        allStocksData,
                        selectedTransactions,
                        selectedWatchList);

                addDataToBarChart(barChartData, stockNames);
            }

            Platform.runLater(() -> {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    var i = 0;
                    for (var stockName : stockNames) {
                        var nodes = barChart.lookupAll(".series" + (i++));

                        for (var node : nodes) {
                            var color = convertStringToHexColor(stockName);
                            node.setStyle("-fx-bar-fill: " + color + ";");
                        }
                    }
                });
            });
        } else {
            showLineChart();

            for(var stock : allStocksData.keySet()) {
                for(var parameterData : allStocksData.get(stock)) {
                    var lineChartData = visualizationDataManager.getLineChartParameterData(parameterData);
                    lineChart.getData().add(lineChartData);
                }
            }
        }
    }

    private void addDataToBarChart(XYChart.Series<String, Number> barChartData, List<String> stockNames) {
        final DecimalFormat df = new DecimalFormat("0.00");

        stockNames.add(barChartData.getName());
        barChart.getData().add(barChartData);

        for(var dataEntry : barChartData.getData()) {
            final Tooltip tooltip = new Tooltip(df.format(dataEntry.getYValue()));
            tooltip.setStyle("-fx-font-size: 15");
            tooltip.setShowDelay(Duration.ZERO);
            Tooltip.install(dataEntry.getNode(), tooltip);
        }
    }

    private void createAlert(String content) {
        alarmIsOpen = true;
        Alert alert = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        alert.setHeaderText("Spalte nicht zugewiesen!");
        PrimaryTabManager.setAlertPosition(alert, stockSelectionTable);

        alert.setOnCloseRequest(dialogEvent -> alarmIsOpen = false);
        alert.show();
    }

    private void showLineChart() {
        lineChart.setVisible(true);
        barChart.setVisible(false);
    }

    private void showBarChart() {
        lineChart.setVisible(false);
        barChart.setVisible(true);
    }

    @Override
    public void resetCharts() {
        lineChart.getData().clear();
        barChart.getData().clear();
    }

    @Override
    public void resetSelections() {
        selectedStocks.clear();
        selectedParameters.clear();
    }

    private String convertStringToHexColor(String isin) {
        return String.format("#%06X", (0xFFFFFF & isin.hashCode()));
    }
}
