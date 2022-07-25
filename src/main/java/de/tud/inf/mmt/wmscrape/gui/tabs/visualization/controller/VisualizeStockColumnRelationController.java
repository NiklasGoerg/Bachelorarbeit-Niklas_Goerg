package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.watchlist.WatchListColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.helper.PropertiesHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * handles the configuration of the column correlations between
 * database columns and necessary fields used fot the visualization
 */
@Controller
public class VisualizeStockColumnRelationController {
    @Autowired
    private CourseColumnRepository courseColumnRepository;
    @Autowired
    private TransactionColumnRepository transactionColumnRepository;
    @Autowired
    private WatchListColumnRepository watchListColumnRepository;

    @FXML
    private ComboBox<String> courseDropDown;
    @FXML
    private ComboBox<String> transactionAmountDropDown;
    @FXML
    private ComboBox<String> watchListBuyCourseDropDown;
    @FXML
    private ComboBox<String> watchListSellCourseDropDown;
    @FXML
    private ComboBox<String> watchListAmountDropDown;

    public static final String stockCourseTableCourseColumn = "WertpapierKursdatenKursSpaltenName";
    public static final String transactionTableAmountColumn = "TransaktionAnzahlSpaltenName";
    public static final String watchListTableAmountColumn = "WatchListeAnzahlSpaltenName";
    public static final String watchListTableBuyCourseColumn = "WatchListeKaufKursSpaltenName";
    public static final String watchListTableSellCourseColumn = "WatchListeVerkaufsKursSpaltenName";

    @FXML
    public void initialize() {
        fillDropDownMenus();
    }

    /**
     * fills the drop down menus with the database columns the user can choose
     */
    private void fillDropDownMenus() {
        courseDropDown.getItems().clear();
        transactionAmountDropDown.getItems().clear();
        watchListBuyCourseDropDown.getItems().clear();
        watchListSellCourseDropDown.getItems().clear();
        watchListAmountDropDown.getItems().clear();

        var columnNames = PropertiesHelper.getProperties(
                stockCourseTableCourseColumn,
                transactionTableAmountColumn,
                watchListTableBuyCourseColumn,
                watchListTableSellCourseColumn,
                watchListTableAmountColumn
        );

        var courseColumns = courseColumnRepository.findAll();
        for (var column : courseColumns) {
            courseDropDown.getItems().add(column.getName());
        }

        var selectedCourseColumn = columnNames.get(stockCourseTableCourseColumn);
        if(courseColumns.stream().anyMatch(c -> c.getName().equals(selectedCourseColumn))) {
            courseDropDown.getSelectionModel().select(selectedCourseColumn);
        }

        var transactionColumns = transactionColumnRepository.findAll();
        for (var column : transactionColumns) {
            transactionAmountDropDown.getItems().add(column.getName());
        }

        var selectedTransactionAmountColumn = columnNames.get(transactionTableAmountColumn);
        if(transactionColumns.stream().anyMatch(c -> c.getName().equals(selectedTransactionAmountColumn))) {
            transactionAmountDropDown.getSelectionModel().select(selectedTransactionAmountColumn);
        }

        var watchListColumns = watchListColumnRepository.findAll();
        for (var column : watchListColumns) {
            watchListAmountDropDown.getItems().add(column.getName());
            watchListBuyCourseDropDown.getItems().add(column.getName());
            watchListSellCourseDropDown.getItems().add(column.getName());
        }

        var selectedWatchListTableAmountColumn = columnNames.get(watchListTableAmountColumn);
        if(watchListColumns.stream().anyMatch(c -> c.getName().equals(selectedWatchListTableAmountColumn))) {
            watchListAmountDropDown.getSelectionModel().select(selectedWatchListTableAmountColumn);
        }

        var selectedWatchListTableBuyCourseColumn = columnNames.get(watchListTableBuyCourseColumn);
        if(watchListColumns.stream().anyMatch(c -> c.getName().equals(selectedWatchListTableBuyCourseColumn))) {
            watchListBuyCourseDropDown.getSelectionModel().select(selectedWatchListTableBuyCourseColumn);
        }

        var selectedWatchListTableSellCourseColumn = columnNames.get(watchListTableSellCourseColumn);
        if(watchListColumns.stream().anyMatch(c -> c.getName().equals(selectedWatchListTableSellCourseColumn))) {
            watchListSellCourseDropDown.getSelectionModel().select(selectedWatchListTableSellCourseColumn);
        }
    }

    /**
     * saves the configured correlations into the user.properties file
     */
    @FXML
    public void saveConfiguration() {
        var isInputValid = true;

        var selectedStockCourseTableCourseColumn = courseDropDown.getSelectionModel().selectedItemProperty().getValue();
        if(selectedStockCourseTableCourseColumn != null) {
            PropertiesHelper.setProperty(stockCourseTableCourseColumn, selectedStockCourseTableCourseColumn);
        } else {
            PrimaryTabManager.decorateField(courseDropDown, "Dieses Feld darf nicht leer sein!", false, true);
            isInputValid = false;
        }

        var selectedTransactionTableAmountColumn = transactionAmountDropDown.getSelectionModel().selectedItemProperty().getValue();
        if(selectedTransactionTableAmountColumn != null) {
            PropertiesHelper.setProperty(transactionTableAmountColumn, selectedTransactionTableAmountColumn);
        } else {
            PrimaryTabManager.decorateField(transactionAmountDropDown, "Dieses Feld darf nicht leer sein!", false, true);
            isInputValid = false;
        }

        var selectedWatchListTableBuyCourseColumn = watchListBuyCourseDropDown.getSelectionModel().selectedItemProperty().getValue();
        if(selectedWatchListTableBuyCourseColumn != null) {
            PropertiesHelper.setProperty(watchListTableBuyCourseColumn, selectedWatchListTableBuyCourseColumn);
        } else {
            PrimaryTabManager.decorateField(watchListBuyCourseDropDown, "Dieses Feld darf nicht leer sein!", false, true);
            isInputValid = false;
        }

        var selectedWatchListTableSellCourseColumn = watchListSellCourseDropDown.getSelectionModel().selectedItemProperty().getValue();
        if(selectedWatchListTableSellCourseColumn != null) {
            PropertiesHelper.setProperty(watchListTableSellCourseColumn, selectedWatchListTableSellCourseColumn);
        } else {
            PrimaryTabManager.decorateField(watchListSellCourseDropDown, "Dieses Feld darf nicht leer sein!", false, true);
            isInputValid = false;
        }

        var selectedWatchListTableAmountColumn = watchListAmountDropDown.getSelectionModel().selectedItemProperty().getValue();
        if(selectedWatchListTableAmountColumn != null) {
            PropertiesHelper.setProperty(watchListTableAmountColumn, selectedWatchListTableAmountColumn);
        } else {
            PrimaryTabManager.decorateField(watchListAmountDropDown, "Dieses Feld darf nicht leer sein!", false, true);
            isInputValid = false;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Die ausgewählten Spaltenzuordnung wurden gespeichert", ButtonType.OK);
        var window = courseDropDown.getScene().getWindow();
        alert.setX(window.getX()+(window.getWidth()/2)-200);
        alert.setY(window.getY()+(window.getHeight()/2)-200);

        alert.showAndWait();

        if(isInputValid) {
            window.hide();
        }
    }
}
