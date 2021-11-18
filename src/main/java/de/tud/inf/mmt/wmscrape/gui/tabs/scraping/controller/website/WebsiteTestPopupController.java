package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingWebsiteTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteTester;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class WebsiteTestPopupController {
    @FXML private Label nextStep;
    @FXML private TextArea logTextArea;
    private SimpleStringProperty logText;
    private WebsiteTester websiteTester;
    private Website website;

    @Autowired
    private ScrapingWebsiteTabController scrapingWebsiteTabController;

    @FXML
    private void initialize() {
        logText = new SimpleStringProperty("");
        logTextArea.textProperty().bind(logText);

        website = scrapingWebsiteTabController.getWebsiteUnpersistedData();
        websiteTester = new WebsiteTester(website, logText);

        nextStep.setText("Browser starten");
    }

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
            case 3, 6 -> nextStep.setText("Cookies verstecken");
            case 4 -> nextStep.setText("Login Informationen ausfüllen");
            case 5 -> nextStep.setText("Einloggen");
            case 7 -> nextStep.setText("Ausloggen");
            case 8 -> nextStep.setText("Browser schließen");
            case 9 -> nextStep.setText("Test beenden");
            default -> handleCancelButton();
        }

    }

    @FXML
    private void handleCancelButton() {
        websiteTester.cancel();
        logTextArea.getScene().getWindow().hide();
    }

}
