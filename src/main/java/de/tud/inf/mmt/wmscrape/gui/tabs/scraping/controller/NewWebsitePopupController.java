package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class NewWebsitePopupController {
    @FXML
    private TextField descriptionField;

    @Autowired
    private ScrapingTabManager scrapingTabManager;
    @Autowired
    private ScrapingWebsiteTabController scrapingWebsiteTabController;

    ValidationSupport dbPathValidation = new ValidationSupport();

    @FXML
    private void initialize() {
        descriptionField.textProperty().addListener((o,ov,nv) -> { if (nv != null) isValidDescription();});
    }

    @FXML
    private void handleCancelButton() {
        descriptionField.getScene().getWindow().hide();
    }

    @FXML
    private void handleConfirmButton() {
        if(!isValidDescription()) {
            return;
        }

        Website website = scrapingTabManager.createNewWebsite(descriptionField.getText());
        scrapingWebsiteTabController.reloadWebsiteList();
        scrapingWebsiteTabController.selectWebsite(website);

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Eine leere Webseitenbeschreibung wurde angelegt", ButtonType.OK);
        alert.setHeaderText("Webseite angelegt!");
        alert.showAndWait();

        descriptionField.getScene().getWindow().hide();
    }

    private boolean isValidDescription() {
        descriptionField.getStyleClass().remove("bad-input");
        descriptionField.setTooltip(null);

        if(descriptionField.getText() == null || descriptionField.getText().isBlank()) {
            descriptionField.setTooltip(scrapingTabManager.createTooltip("Dieses Feld darf nicht leer sein!"));
            descriptionField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }
}
