package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public abstract class VisualizationTabControllerTab {
    protected CheckBox normalizeCheckbox;
    protected DatePicker startDatePicker;
    protected DatePicker endDatePicker;
    protected ComboBox<String> transactionAmountDropDown;
    protected ComboBox<String> watchListAmountDropDown;

    public void setTools(CheckBox normalizeCheckbox, DatePicker startDatePicker, DatePicker endDatePicker, ComboBox<String> transactionAmountDropDown, ComboBox<String> watchListAmountDropDown) {
        this.normalizeCheckbox = normalizeCheckbox;
        this.startDatePicker = startDatePicker;
        this.endDatePicker = endDatePicker;
        this.transactionAmountDropDown = transactionAmountDropDown;
        this.watchListAmountDropDown = watchListAmountDropDown;
    }

    public void initializeUI() {
        prepareSelectionTables();
        fillSelectionTables();
        prepareCharts();
    }

    public abstract void prepareCharts();
    public abstract void prepareSelectionTables();
    public void prepareCanvas() {}

    public abstract void fillSelectionTables();
    public abstract void loadData(LocalDate startDate, LocalDate endDate);

    public abstract void resetCharts();
    public abstract void resetSelections();
}
