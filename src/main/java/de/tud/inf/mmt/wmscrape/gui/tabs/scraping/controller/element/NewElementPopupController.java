package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ScrapingTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Lazy
public class NewElementPopupController {
    @FXML private TextField descriptionField;
    @FXML private ChoiceBox<ContentType> contentTypeChoiceBox;
    @FXML private ChoiceBox<MultiplicityType> multiplicityChoiceBox;

    @Autowired
    private ScrapingTabManager scrapingTabManager;
    @Autowired
    private ScrapingElementsTabController scrapingElementsTabController;

    @FXML
    private void initialize() {
        descriptionField.textProperty().addListener((o,ov,nv) -> { if (nv != null) isValidDescription();});
        contentTypeChoiceBox.getItems().addAll(ContentType.values());
        multiplicityChoiceBox.getItems().addAll(MultiplicityType.values());
        contentTypeChoiceBox.setValue(ContentType.AKTIENKURS);
        multiplicityChoiceBox.setValue(MultiplicityType.TABELLE);
    }

    @FXML
    private void handleSaveButton() {
        if(!isValidDescription()) {
            return;
        }

        WebsiteElement element = scrapingTabManager.createNewElement(
                descriptionField.getText(),
                contentTypeChoiceBox.getValue(),
                multiplicityChoiceBox.getValue());
        scrapingElementsTabController.reloadElementList();
        scrapingElementsTabController.selectElement(element);

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Eine neues Webseiten-Element wurde angelegt", ButtonType.OK);
        alert.setHeaderText("Element angelegt!");
        var window = descriptionField.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
        alert.showAndWait();

        window.hide();

    }

    @FXML
    private void handleCancelButton() {
        descriptionField.getScene().getWindow().hide();
    }

    private boolean isValidDescription() {
        descriptionField.getStyleClass().remove("bad-input");
        descriptionField.setTooltip(null);

        if(descriptionField.getText() == null || descriptionField.getText().isBlank()) {
            descriptionField.setTooltip(PrimaryTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            descriptionField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }
}
