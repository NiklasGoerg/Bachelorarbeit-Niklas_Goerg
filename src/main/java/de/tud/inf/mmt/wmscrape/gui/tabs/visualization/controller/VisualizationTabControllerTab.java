package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import javafx.scene.control.*;

import java.time.LocalDate;

public abstract class VisualizationTabControllerTab {
    protected CheckBox normalizeCheckbox;
    protected DatePicker startDatePicker;
    protected DatePicker endDatePicker;
    protected boolean alarmIsOpen = false;

    /**
     * sets the objects of the toolbar in order to let the sub-controller access and use them
     * @param normalizeCheckbox
     * @param startDatePicker
     * @param endDatePicker
     */
    public void setTools(CheckBox normalizeCheckbox, DatePicker startDatePicker, DatePicker endDatePicker) {
        this.normalizeCheckbox = normalizeCheckbox;
        this.startDatePicker = startDatePicker;
        this.endDatePicker = endDatePicker;
    }

    /**
     * initializes the ui for the current controller
     */
    public void initializeUI() {
        prepareSelectionTables();
        fillSelectionTables();
        prepareCharts();
    }

    /**
     * creates an alert for the user e.g. when some visualization errors occur
     * @param content message to be displayed to the user
     */
    public void createAlert(String content) {
        alarmIsOpen = true;
        Alert alert = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        alert.setHeaderText("Spalte nicht zugewiesen!");
        PrimaryTabManager.setAlertPosition(alert, normalizeCheckbox);

        alert.setOnCloseRequest(dialogEvent -> alarmIsOpen = false);
        alert.show();
    }

    /**
     * used to setup the charts e.g. setting lower an upper bounds or preparing the axis
     */
    public abstract void prepareCharts();

    /**
     * used to setup the selection tables e.g. creating the table columns and registering listeners
     */
    public abstract void prepareSelectionTables();

    /**
     * fills the selection tables with data
     */
    public abstract void fillSelectionTables();

    /**
     * loads the data from the database which should be visualized
     * @param startDate represents the lower bound of the user selected time span
     * @param endDate represents the upper bound of the user selected time span
     */
    public abstract void loadData(LocalDate startDate, LocalDate endDate);

    /**
     * resets the charts and removes any data currently visualized
     */
    public abstract void resetCharts();
}
