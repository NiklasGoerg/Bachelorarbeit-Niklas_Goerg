package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingWebsiteTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivatedUrl;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.WebsiteManager;
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
    private WebsiteManager websiteManager;
    private Website website;
    private int step;

    @Autowired
    private ScrapingWebsiteTabController scrapingWebsiteTabController;

    @FXML
    private void initialize() {
        logText = new SimpleStringProperty("");
        logTextArea.textProperty().bind(logText);

        website = scrapingWebsiteTabController.getSelectedWebsite();
        websiteManager = new WebsiteManager(website, false, 5, logText);

        step = 0;
        nextStep.setText("Browser starten");
    }

    @FXML
    private void handleNextStepButton() {

        // if closed stop test
        if(step>0 && websiteManager.getDriver() == null) handleCancelButton();

        if(step==0) {
            websiteManager.startBrowser();
            nextStep.setText("Webseite laden");
        } else if(step==1) {
            websiteManager.loadPage();

            if(website.getCookieAcceptIdentType() == IdentTypeDeactivated.DEAKTIVIERT) {
                if(website.getCookieHideIdentType() == IdentTypeDeactivated.DEAKTIVIERT) {
                    nextStep.setText("Login Informationen ausfüllen");
                    step=4;
                    return;
                }
                nextStep.setText("Cookies verstecken");
                step=3;
                return;
            }

            nextStep.setText("Cookies akzeptieren");
        } else if(step==2 ) {
            websiteManager.acceptCookies();

            if(website.getCookieHideIdentType() == IdentTypeDeactivated.DEAKTIVIERT) {
                nextStep.setText("Login Informationen ausfüllen");
                step+=1;
            } else nextStep.setText("Cookies verstecken");
        } else if(step==3) {
            websiteManager.hideCookies();
            nextStep.setText("Login Informationen ausfüllen");
        } else if(step==4) {
            websiteManager.fillLoginInformation();
            nextStep.setText("Einloggen");
        } else if(step==5) {
            websiteManager.login();

            if(website.getCookieHideIdentType() == IdentTypeDeactivated.DEAKTIVIERT) {
                nextStep.setText("Ausloggen");
                step+=1;

                if(website.getLogoutIdentType() == IdentTypeDeactivatedUrl.DEAKTIVIERT) {
                    nextStep.setText("Browser schließen");
                    step++;
                }
            } else nextStep.setText("Cookies verstecken");

        } else if(step==6) {
            websiteManager.hideCookies();
            nextStep.setText("Ausloggen");
        } else if(step==7) {
            websiteManager.logout();
            nextStep.setText("Browser schließen");
        } else if(step==8) {
            websiteManager.quit();
            nextStep.setText("Test beenden");
        } else {
            handleCancelButton();
        }
        step++;
    }

    @FXML
    private void handleCancelButton() {
        websiteManager.quit();
        logTextArea.getScene().getWindow().hide();
    }

}
