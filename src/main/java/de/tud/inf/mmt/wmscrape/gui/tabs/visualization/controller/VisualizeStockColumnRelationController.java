package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.watchlist.WatchListColumnRepository;
import de.tud.inf.mmt.wmscrape.helper.PropertiesHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
    private ComboBox<String> watchListCourseDropDown;
    @FXML
    private ComboBox<String> watchListAmountDropDown;

    public static final String stockCourseTableCourseColumn = "WertpapierKursdatenKursSpaltenName";
    public static final String transactionTableAmountColumn = "TransaktionAnzahlSpaltenName";
    public static final String watchListTableCourseColumn = "WatchListeAnzahlSpaltenName";
    public static final String watchListTableAmountColumn = "WatchListeKursSpaltenName";

    @FXML
    public void initialize() {
        fillDropDownMenus();
    }

    private void fillDropDownMenus() {
        courseDropDown.getItems().clear();
        transactionAmountDropDown.getItems().clear();
        watchListCourseDropDown.getItems().clear();
        watchListAmountDropDown.getItems().clear();

        var columnNames = PropertiesHelper.getProperties(
                stockCourseTableCourseColumn,
                transactionTableAmountColumn,
                watchListTableCourseColumn,
                watchListTableAmountColumn
        );

        for (var column : courseColumnRepository.findAll()) {
            courseDropDown.getItems().add(column.getName());
        }
        courseDropDown.getSelectionModel().select(columnNames.get(stockCourseTableCourseColumn));

        for (var column : transactionColumnRepository.findAll()) {
            transactionAmountDropDown.getItems().add(column.getName());
        }
        transactionAmountDropDown.getSelectionModel().select(columnNames.get(transactionTableAmountColumn));

        for (var column : watchListColumnRepository.findAll()) {
            watchListAmountDropDown.getItems().add(column.getName());
            watchListCourseDropDown.getItems().add(column.getName());
        }
        watchListAmountDropDown.getSelectionModel().select(columnNames.get(watchListTableCourseColumn));
        watchListCourseDropDown.getSelectionModel().select(columnNames.get(watchListTableAmountColumn));
    }

    @FXML
    public void saveConfiguration() {
        PropertiesHelper.setProperty(stockCourseTableCourseColumn, courseDropDown.getSelectionModel().selectedItemProperty().getValue());
        PropertiesHelper.setProperty(transactionTableAmountColumn, transactionAmountDropDown.getSelectionModel().selectedItemProperty().getValue());
        PropertiesHelper.setProperty(watchListTableCourseColumn, watchListCourseDropDown.getSelectionModel().selectedItemProperty().getValue());
        PropertiesHelper.setProperty(watchListTableAmountColumn, watchListAmountDropDown.getSelectionModel().selectedItemProperty().getValue());

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Spaltenzuordnung gespeichert", ButtonType.OK);
        var window = courseDropDown.getScene().getWindow();
        alert.setX(window.getX()+(window.getWidth()/2)-200);
        alert.setY(window.getY()+(window.getHeight()/2)-200);

        alert.showAndWait();

        window.hide();
    }
}
