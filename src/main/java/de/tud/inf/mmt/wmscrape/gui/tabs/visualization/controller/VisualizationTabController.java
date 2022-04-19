package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.WMScrape;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisualizationTabController {
    @Autowired
    private ApplicationContext applicationContext ;

    @FXML
    private CheckBox normalizeCheckbox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TabPane tabPane;

    private VisualizationTabControllerTab currentTab;

    @FXML
    public void initialize() {
        try {
            var courseLoader = getTabLoader("gui/tabs/visualization/controller/visualizeCourseTab.fxml");
            Parent courseRoot = courseLoader.load();
            VisualizationCourseTabController controller =  courseLoader.getController();

            var courseTab = new Tab("Kursdaten", courseRoot);
            courseTab.selectedProperty().addListener((o,ov,nv) -> {
                currentTab = controller;
                currentTab.setTools(normalizeCheckbox, startDatePicker, endDatePicker);
                normalizeCheckbox.setSelected(true);
            });
            tabPane.getTabs().add(courseTab);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            var stockLoader = getTabLoader("gui/tabs/visualization/controller/visualizeStockTab.fxml");
            Parent stockRoot = stockLoader.load();
            VisualizationStockTabController controller =  stockLoader.getController();

            var stockTab = new Tab("Wertpapier-Parameter", stockRoot);
            stockTab.selectedProperty().addListener((o,ov,nv) -> {
                currentTab = controller;
                currentTab.setTools(normalizeCheckbox, startDatePicker, endDatePicker);
                normalizeCheckbox.setSelected(false);
            });
            tabPane.getTabs().add(stockTab);
        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareTools();
    }

    public FXMLLoader getTabLoader(String ressourceUri) {
        var tabRessourceUri = WMScrape.class.getResource(ressourceUri);

        if(tabRessourceUri == null) return null;

        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(applicationContext::getBean);
        loader.setLocation(tabRessourceUri);
        return loader;
    }

    @FXML
    public void openNewWindow() {

        try {
            var ressourceUri = WMScrape.class.getResource("gui/tabs/visualization/controller/visualizeTab.fxml");

            if(ressourceUri == null) return;

            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load(ressourceUri.openStream());
            VisualizationTabController controller =  loader.getController();

            controller.setNormalize(normalizeCheckbox.isSelected());
            controller.setStartDate(startDatePicker.getValue());
            controller.setEndDate(endDatePicker.getValue());

            Stage stage = new Stage();
            stage.setTitle("Darstellung");
            stage.setScene(new Scene(root, 1337.0, 756.0));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareTools() {
        normalizeCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            currentTab.loadData(startDatePicker.getValue(), endDatePicker.getValue());
        });

        startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
            currentTab.loadData(startDatePicker.getValue(), endDatePicker.getValue());
        });

        endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
            currentTab.loadData(startDatePicker.getValue(), endDatePicker.getValue());
        });
    }

    public void resetCharts() {
        currentTab.resetCharts();
    }

    public void fillSelectionTables() {
        currentTab.fillSelectionTables();
    }

    public void setNormalize(boolean normalize) {
        normalizeCheckbox.setSelected(normalize);
    }

    public void setStartDate(LocalDate startDate) {
        startDatePicker.setValue(startDate);
    }

    public void setEndDate(LocalDate endDate) {
        endDatePicker.setValue(endDate);
    }
}
