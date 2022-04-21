package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public abstract class VisualizationTabControllerTab {
    protected CheckBox normalizeCheckbox;
    protected DatePicker startDatePicker;
    protected DatePicker endDatePicker;

    public void setTools(CheckBox normalizeCheckbox, DatePicker startDatePicker, DatePicker endDatePicker) {
        this.normalizeCheckbox = normalizeCheckbox;
        this.startDatePicker = startDatePicker;
        this.endDatePicker = endDatePicker;
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
