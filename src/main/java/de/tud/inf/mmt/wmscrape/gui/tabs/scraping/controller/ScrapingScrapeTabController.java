package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ScrapingManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.WebsiteManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ScrapingScrapeTabController {

    @Autowired
    ScrapingManager scrapingManager;
    @Autowired
    WebsiteManager websiteManager;

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
    @FXML private Button startButton;

    private final ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems = FXCollections.observableHashMap();
    private static SimpleStringProperty logText;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() {
        logText = new SimpleStringProperty("");
        logArea.clear();
        logArea.textProperty().bind(logText);

        // twice works better. might be because of the delayed threads
        logText.addListener(x -> logArea.setScrollTop(Double.MAX_VALUE));
        logArea.textProperty().addListener(x -> logArea.setScrollTop(Double.MAX_VALUE));

        delayMinSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(2, 50, 5, 0.5));
        delayMaxSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(2, 300, 7.5, 0.5));

        delayMinSpinner.valueProperty().addListener((o,ov,nv) -> {
            if(nv > delayMaxSpinner.getValue()) delayMaxSpinner.getValueFactory().setValue(nv);
        });

        delayMaxSpinner.valueProperty().addListener((o,ov,nv) -> {
            if(nv < delayMinSpinner.getValue()) delayMinSpinner.getValueFactory().setValue(nv);
        });

        waitProgress.progressProperty().addListener((o, ov, nv) -> waitProgress.setVisible(nv.doubleValue() > 0));

        makeStartVisible(true);
        selectionProgress.progressProperty().addListener((o,ov,nv) -> {
            if(nv.doubleValue() > 0.1) { makeStartVisible(false);}
        });

        makeContinueVisible(false);
        elementProgress.progressProperty().addListener((o,ov,nv) -> {
            if(nv.doubleValue() > 0.1) { makeContinueVisible(true);}
        });

        websiteProgress.progressProperty().addListener((o,ov,nv) -> {
            if(allDone()) {
                makeContinueVisible(false);
                makeStartVisible(true);
            } });

        waitSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 50, 5, 0.25));
        headlessCheckBox.setSelected(false);
        pauseCheckBox.setSelected(false);
        //filling the tree and setting the stored settings
        restoreSettings();

        headlessCheckBox.selectedProperty().addListener((o,ov,nv) -> {
            if(nv) pauseCheckBox.setSelected(false);
        });
        pauseCheckBox.selectedProperty().addListener((o,ov,nv) -> {
            if(nv) headlessCheckBox.setSelected(false);
        });
    }

    /**
     * starts the scraping task and combines the progress bar properties with the ones from the task
     */
    @FXML
    public void handleStartButton() {
        scrapingManager.startScrape(
                delayMinSpinner.getValue(),
                delayMaxSpinner.getValue(),
                waitSpinner.getValue(),
                pauseCheckBox.isSelected(),
                logText,headlessCheckBox.isSelected(),
                checkedItems,
                false);

        scrapingManager.bindProgressBars(websiteProgress, elementProgress, selectionProgress, waitProgress);
        saveSettings();
    }


    /**
     * continues the scraping process if the task was paused
     */
    @FXML
    private void handleNextButton() {
        scrapingManager.continueScrape(
                delayMinSpinner.getValue(),
                delayMaxSpinner.getValue(),
                waitSpinner.getValue(),
                pauseCheckBox.isSelected());
    }

    /**
     * called when loading the fxml file
     *
     * <li>binds the various properties of buttons an elements to functions</li>
     * <li></li>
     */
    @FXML
    private void handleAbortButton() {
        scrapingManager.cancelScrape();
        resetProgressBars();
    }

    /**
     *
     * @return true if all selected elements have been processed
     */
    private boolean allDone() {
        return websiteProgress.getProgress() >= 1 && elementProgress.getProgress() >= 1;
    }

    public void resetProgressBars() {
        waitProgress.setProgress(0);
        websiteProgress.setProgress(0);
        elementProgress.setProgress(0);
        selectionProgress.setProgress(0);
        makeContinueVisible(false);
        makeStartVisible(true);
    }

    /**
     * only shows the continue button if needed
     * @param b if true show the button
     */
    private void makeContinueVisible(boolean b){
        if(b && !headlessCheckBox.isSelected()) {
            continueButton.setVisible(true);
            continueButton.setManaged(true);
        } else {
            continueButton.setVisible(false);
            continueButton.setManaged(false);
        }
    }


    private void makeStartVisible(boolean b){
        startButton.setVisible(b);
        startButton.setManaged(b);
    }

    public void refresh() {
        handleAbortButton();
        restoreSettings();
    }

    /**
     * restores the scraping settings from properties
     */
    public void restoreSettings() {
        Properties p = scrapingManager.getProperties();
        delayMinSpinner.getValueFactory().setValue(Double.valueOf(p.getProperty("scraping.delay.min", "4")));
        delayMaxSpinner.getValueFactory().setValue(Double.valueOf(p.getProperty("scraping.delay.max", "6.5")));
        waitSpinner.getValueFactory().setValue(Double.valueOf(p.getProperty("scraping.wait", "5")));
        headlessCheckBox.setSelected(Boolean.parseBoolean(p.getProperty("scraping.headless", "false")));
        pauseCheckBox.setSelected(Boolean.parseBoolean(p.getProperty("scraping.pause", "false")));

        /* using the hash value of the elements separated by comma */
        int[] r = Arrays.stream(p.getProperty("scraping.selected", "0").split(","))
                .mapToInt(Integer::parseInt).toArray();
        Set<Integer> restoredSelected = Arrays.stream(r).boxed().collect(Collectors.toSet());

        // replacing the selection tree
        var tree = scrapingManager.createSelectionTree(checkedItems ,restoredSelected, false);
        tree.setPadding(new Insets(15,1,1,1));
        borderPane.setCenter(tree);
    }

    /**
     * stores the scraping settings from properties
     */
    private void saveSettings() {
        Properties p = scrapingManager.getProperties();
        p.setProperty("scraping.delay.min", String.valueOf(delayMinSpinner.getValue()));
        p.setProperty("scraping.delay.max", String.valueOf(delayMaxSpinner.getValue()));
        p.setProperty("scraping.wait", String.valueOf(waitSpinner.getValue()));
        p.setProperty("scraping.headless", String.valueOf(headlessCheckBox.isSelected()));
        p.setProperty("scraping.pause", String.valueOf(pauseCheckBox.isSelected()));

        StringBuilder hashes = new StringBuilder("0");
        // saving all WebsiteElements in subList
        for (Map.Entry<Website, ObservableSet<WebsiteElement>> mapEntry : checkedItems.entrySet()) {
            mapEntry.getValue().forEach(element -> hashes.append(",").append(element.hashCode()));
        }
        p.setProperty("scraping.selected", String.valueOf(hashes));

        scrapingManager.saveProperties();
    }
}
