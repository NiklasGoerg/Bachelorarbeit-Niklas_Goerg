package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ParameterSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management.VisualizationDataManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
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
public class VisualizationStockTabController extends VisualizationTabControllerTab {
    @FXML
    private TableView<StockSelection> stockSelectionTable;
    @FXML
    private TableView<ParameterSelection> parameterSelectionTable;
    @FXML
    private StackPane stackPane;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private NumberAxis lineXAxis;
    @FXML
    private BarChart<Number, String> barChart;
    @FXML
    private CategoryAxis barXAxis;
    @FXML
    private Canvas canvas;

    @Autowired
    private VisualizationDataManager visualizationDataManager;

    private final List<StockSelection> selectedStocks = new ArrayList<>();
    private final List<ParameterSelection> selectedParameters = new ArrayList<>();

    @FXML
    public void initialize() {
        initializeUI();
    }

    @Override
    public void prepareCharts() {
        lineChart.setVisible(true);
        barChart.setVisible(false);

        lineChart.setAnimated(false);

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

        isinCol.setEditable(false);
        nameCol.setEditable(false);
        isSelectedCol.setEditable(true);

        isinCol.setPrefWidth(100);
        nameCol.setPrefWidth(100);
        isSelectedCol.setPrefWidth(70);

        stockSelectionTable.getColumns().add(isinCol);
        stockSelectionTable.getColumns().add(nameCol);
        stockSelectionTable.getColumns().add(isSelectedCol);
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
        stockSelectionTable.getItems().clear();

        var stocks = visualizationDataManager.getStocksWithParameterData();

        for (var stockSelection : stocks) {
            stockSelectionTable.getItems().add(stockSelection);
        }
    }

    private void fillParameterSelectionTable() {
        parameterSelectionTable.getItems().clear();

        var stocks = visualizationDataManager.getParameters();

        for (var stockSelection : stocks) {
            parameterSelectionTable.getItems().add(stockSelection);
        }
    }

    @Override
    public void loadData(LocalDate startDate, LocalDate endDate) {
        resetCharts();

        if (selectedStocks.size() == 0 || selectedParameters.size() == 0) return;

        var firstSelectedStock = (StockSelection) selectedStocks.get(0);

        for (var tableItem : selectedStocks) {
            for(var parameter : selectedParameters) {
                if (!tableItem.isSelected().getValue()) continue;

                var data = visualizationDataManager.getParameterDataForIsin(tableItem.getIsin(), parameter, startDate, endDate);

                if (data == null || data.getData().size() == 0) return;

                data.setName(tableItem.getName());

                if (normalizeCheckbox.isSelected() && !tableItem.getIsin().equals(firstSelectedStock.getIsin())) {
                    data = visualizationDataManager.normalizeData(data, firstSelectedStock);
                }

                lineChart.getData().addAll(data);
            }
        }
    }

    @Override
    public void resetCharts() {
        lineChart.getData().clear();
        barChart.getData().clear();
    }
}
