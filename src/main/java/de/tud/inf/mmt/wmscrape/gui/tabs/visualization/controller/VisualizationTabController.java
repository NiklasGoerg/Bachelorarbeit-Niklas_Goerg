package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.watchlist.WatchListColumnRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;

@Controller
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisualizationTabController {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WatchListColumnRepository watchListColumnRepository;

    @Autowired
    private TransactionColumnRepository transactionColumnRepository;

    @FXML
    private CheckBox normalizeCheckbox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private HBox dropDownMenuParent;
    @FXML
    private ComboBox<String> transactionAmountDropDown;
    @FXML
    private ComboBox<String> watchListAmountDropDown;

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
                currentTab.setTools(normalizeCheckbox, startDatePicker, endDatePicker, transactionAmountDropDown, watchListAmountDropDown);
                normalizeCheckbox.setSelected(true);

                dropDownMenuParent.setVisible(false);
                dropDownMenuParent.setManaged(false);

                normalizeCheckbox.setDisable(false);
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
                currentTab.setTools(normalizeCheckbox, startDatePicker, endDatePicker, transactionAmountDropDown, watchListAmountDropDown);

                normalizeCheckbox.setSelected(false);

                dropDownMenuParent.setVisible(true);
                dropDownMenuParent.setManaged(true);

                normalizeCheckbox.setDisable(true);

                fillDropDownMenus();
            });
            tabPane.getTabs().add(stockTab);
        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareTools();
    }

    private void prepareDropDownMenus() {
        transactionAmountDropDown.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null) {
                setColumnNameProperty("TransaktionAnzahlSpaltenName", newValue);
            }
        });

        watchListAmountDropDown.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null) {
                setColumnNameProperty("WatchListeAnzahlSpaltenName", newValue);
            }
        });
    }

    public void fillDropDownMenus() {
        transactionAmountDropDown.getItems().clear();
        watchListAmountDropDown.getItems().clear();

        for(var column : transactionColumnRepository.findAll()) {
            transactionAmountDropDown.getItems().add(column.getName());
        }

        transactionAmountDropDown.getSelectionModel().select(getColumnNameProperty("TransaktionAnzahlSpaltenName"));

        for(var column : watchListColumnRepository.findAll()) {
            watchListAmountDropDown.getItems().add(column.getName());
        }

        watchListAmountDropDown.getSelectionModel().select(getColumnNameProperty("WatchListeAnzahlSpaltenName"));
    }

    public FXMLLoader getTabLoader(String ressourceUri) {
        var tabRessourceUri = WMScrape.class.getResource(ressourceUri);

        if(tabRessourceUri == null) return null;

        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(applicationContext::getBean);
        loader.setLocation(tabRessourceUri);
        return loader;
    }

    private String getColumnNameProperty(String propertyName) {
        Properties properties = new Properties();
        String property = null;

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            property = properties.getProperty(propertyName, null);
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return property;
    }

    private void setColumnNameProperty(String propertyName, String columnName) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            properties.setProperty(propertyName, columnName);
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    @FXML
    public void resetDatePicker() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
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
        normalizeCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> currentTab.loadData(startDatePicker.getValue(), endDatePicker.getValue()));

        startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> currentTab.loadData(startDatePicker.getValue(), endDatePicker.getValue()));

        endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> currentTab.loadData(startDatePicker.getValue(), endDatePicker.getValue()));

        fillDropDownMenus();
        prepareDropDownMenus();
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
