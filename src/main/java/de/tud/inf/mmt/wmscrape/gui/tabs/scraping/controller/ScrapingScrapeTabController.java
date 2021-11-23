package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebRepresentation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ScrapingTabManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.HashMap;

@Controller
public class ScrapingScrapeTabController {

    @Autowired
    ScrapingTabManager scrapingTabManager;

    @FXML private TextArea logArea;
    @FXML private Spinner<Double> delayMinSpinner;
    @FXML private Spinner<Double> delayMaxSpinner;
    @FXML private Spinner<Double> waitSpinner;
    @FXML private CheckBox headlessCheckBox;
    @FXML private CheckBox pauseCheckBox;
    @FXML private BorderPane borderPane;

    private final ObservableMap<Website, ObservableList<WebsiteElement>> checkedItems = FXCollections.observableMap(new HashMap<>());
    private static SimpleStringProperty logText;

    @FXML
    private void initialize() {
        logText = new SimpleStringProperty("");
        logArea.clear();
        logArea.textProperty().bind(logText);

        // twice works better. might be because of the delayed threads
        logText.addListener(x -> logArea.setScrollTop(Double.MAX_VALUE));
        logArea.textProperty().addListener(x -> logArea.setScrollTop(Double.MAX_VALUE));

        delayMinSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, 1, 0.25));
        delayMaxSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 50, 3, 0.25));
        waitSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 50, 5, 0.25));
        headlessCheckBox.setSelected(false);
        pauseCheckBox.setSelected(true);
        borderPane.setCenter(getSelectionTree());

        headlessCheckBox.selectedProperty().addListener((o,ov,nv) -> {
            if(nv) pauseCheckBox.setSelected(false);
        });
    }

    @FXML
    public void handleStartButton() {
        scrapingTabManager.startScrape(
                delayMinSpinner.getValue(),
                delayMaxSpinner.getValue(),
                waitSpinner.getValue(),
                pauseCheckBox.isSelected(),
                logText,headlessCheckBox.isSelected(),
                checkedItems);
    }


    @FXML
    private void handleNextButton() {
        scrapingTabManager.continueScrape(
                delayMinSpinner.getValue(),
                delayMaxSpinner.getValue(),
                waitSpinner.getValue(),
                pauseCheckBox.isSelected());
    }

    @FXML
    private void handleAbortButton() {
        scrapingTabManager.cancelScrape();
    }

    public TreeView<WebRepresentation<?>> getSelectionTree() {
        return scrapingTabManager.initSelectionTree(checkedItems);
    }
}
