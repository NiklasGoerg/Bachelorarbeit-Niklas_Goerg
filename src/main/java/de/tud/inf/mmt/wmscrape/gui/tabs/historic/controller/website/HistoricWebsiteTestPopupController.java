package de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.HistoricWebsiteTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.historic.management.website.HistoricWebsiteTester;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingWebsiteTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteTester;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * used for skipping through all login and logout steps usually done by the scraper
 */
@Controller
public class HistoricWebsiteTestPopupController {
    @FXML
    private Label nextStep;
    @FXML private TextArea logTextArea;
    private HistoricWebsiteTester websiteTester;

    @Autowired
    private HistoricWebsiteTabController historicWebsiteTabController;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() {
        SimpleStringProperty logText = new SimpleStringProperty("");
        logTextArea.textProperty().bind(logText);

        Website website = historicWebsiteTabController.getWebsiteUnpersistedData();
        websiteTester = new HistoricWebsiteTester(website, logText);

        nextStep.setText("Browser starten");
    }

    /**
     * does the next step in {@link de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteTester}
     */
    @FXML
    private void handleNextStepButton() {

        boolean done = websiteTester.doNextStep();
        if(done) {
            handleCancelButton();
            return;
        }

        int step = websiteTester.getStep();

        // next step
        switch (step) {
            case 1 -> nextStep.setText("Webseite laden");
            case 2 -> nextStep.setText("Cookies akzeptieren");
            case 3 -> nextStep.setText("Login Informationen ausfüllen");
            case 4 -> nextStep.setText("Einloggen");
            case 5 -> nextStep.setText("Navigiere auf Such-Seite");
            case 6 -> nextStep.setText("Suche nach DAX40");
            case 7 -> nextStep.setText("Navigiere zu historischen Daten");
            case 8 -> nextStep.setText("Stelle Datum ein");
            case 9 -> nextStep.setText("Lade historische Daten");
            case 10 -> nextStep.setText("Ausloggen");
            case 11 -> nextStep.setText("Browser schließen");
            case 12 -> nextStep.setText("Test beenden");
            default -> handleCancelButton();
        }

    }

    @FXML
    private void handleCancelButton() {
        websiteTester.cancel();
        logTextArea.getScene().getWindow().hide();
    }

}
