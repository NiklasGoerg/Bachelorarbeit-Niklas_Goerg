package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.*;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisualizationStockTabController extends VisualizationTabControllerTab {
    @FXML
    private TableView<StockSelection> stockSelectionTable;
    @FXML
    private TableView<StockSelection> parameterSelectionTable;
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

    @FXML
    public void initialize() {
        initializeUI();
    }

    @Override
    public void prepareCharts() {
        lineChart.setVisible(true);
        barChart.setVisible(false);
    }

    @Override
    public void prepareSelectionTables() {

    }

    @Override
    public void fillSelectionTables() {

    }

    @Override
    public void loadData(LocalDate startDate, LocalDate endDate) {

    }

    @Override
    public void resetCharts() {

    }
}
