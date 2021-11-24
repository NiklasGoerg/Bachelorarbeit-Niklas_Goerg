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
import javafx.geometry.Insets;
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
    @FXML private ProgressBar websiteProgress;
    @FXML private ProgressBar elementProgress;
    @FXML private ProgressBar selectionProgress;
    @FXML private ProgressIndicator waitProgress;
    @FXML private Button continueButton;

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

        delayMinSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(2, 50, 1, 0.5));
        delayMaxSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(2, 300, 3, 0.5));

        delayMinSpinner.valueProperty().addListener((o,ov,nv) -> {
            if(nv > delayMaxSpinner.getValue()) delayMaxSpinner.getValueFactory().setValue(nv);
        });

        delayMaxSpinner.valueProperty().addListener((o,ov,nv) -> {
            if(nv < delayMinSpinner.getValue()) delayMinSpinner.getValueFactory().setValue(nv);
        });

        waitProgress.progressProperty().addListener((o, ov, nv) -> waitProgress.setVisible(nv.doubleValue() > 0));

        makeContinueVisible(false);
        elementProgress.progressProperty().addListener((o,ov,nv) -> {
            if(nv.doubleValue() > 0.1) {
                makeContinueVisible(true);
            }
        });

        websiteProgress.progressProperty().addListener((o,ov,nv) -> {if(allDone()) makeContinueVisible(false); });

        waitSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 50, 5, 0.25));
        headlessCheckBox.setSelected(false);
        pauseCheckBox.setSelected(true);
        borderPane.setCenter(getSelectionTree());

        headlessCheckBox.selectedProperty().addListener((o,ov,nv) -> {
            if(nv) pauseCheckBox.setSelected(false);
        });
        pauseCheckBox.selectedProperty().addListener((o,ov,nv) -> {
            if(nv) headlessCheckBox.setSelected(false);
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

        scrapingTabManager.bindProgressBars(websiteProgress, elementProgress, selectionProgress, waitProgress);
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
        resetProgressBars();
    }

    private TreeView<WebRepresentation<?>> getSelectionTree() {
        var tree = scrapingTabManager.initSelectionTree(checkedItems);
        tree.setPadding(new Insets(15,1,1,1));
        return tree;
    }

    private boolean allDone() {
        return websiteProgress.getProgress() >= 1 && elementProgress.getProgress() >= 1 && selectionProgress.getProgress() >= 1;
    }

    private void resetProgressBars() {
        waitProgress.setProgress(0);
        websiteProgress.setProgress(0);
        elementProgress.setProgress(0);
        selectionProgress.setProgress(0);
        makeContinueVisible(false);
    }

    private void makeContinueVisible(boolean b){
        if(b && !headlessCheckBox.isSelected()) {
            continueButton.setVisible(true);
            continueButton.setManaged(true);
        } else {
            continueButton.setVisible(false);
            continueButton.setManaged(false);
        }
    }
}
