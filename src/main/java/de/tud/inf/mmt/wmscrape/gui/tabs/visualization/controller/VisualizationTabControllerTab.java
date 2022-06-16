package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import javafx.scene.control.*;

import java.time.LocalDate;

public abstract class VisualizationTabControllerTab {
    protected CheckBox normalizeCheckbox;
    protected DatePicker startDatePicker;
    protected DatePicker endDatePicker;
    protected boolean alarmIsOpen = false;

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

    public void createAlert(String content) {
        alarmIsOpen = true;
        Alert alert = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        alert.setHeaderText("Spalte nicht zugewiesen!");
        PrimaryTabManager.setAlertPosition(alert, normalizeCheckbox);

        alert.setOnCloseRequest(dialogEvent -> alarmIsOpen = false);
        alert.show();
    }

    public abstract void prepareCharts();
    public abstract void prepareSelectionTables();

    public abstract void fillSelectionTables();
    public abstract void loadData(LocalDate startDate, LocalDate endDate);

    public abstract void resetCharts();
}
