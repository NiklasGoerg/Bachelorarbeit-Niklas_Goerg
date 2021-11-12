package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class ScrapingElementsTabController {

    @FXML private ListView<WebsiteElement> elementList;
    @FXML private ChoiceBox<Website> websiteChoiceBox;
    @FXML private TextField urlField;
    @FXML private BorderPane subPane;

    @Autowired
    private ScrapingTabManager scrapingTabManager;
    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private NewElementPopupController newElementPopupController;
    @Autowired
    private SingleExchangeSubController singleExchangeSubController;
    @Autowired
    private SingleCourseOrStockSubController singleCourseOrStockSubController;
    @Autowired
    private TableExchangeSubController tableExchangeSubController;
    @Autowired
    private TableStockSubController tableStockSubController;
    @Autowired
    private TableCourseSubController tableCourseSubController;

    private ObservableList<WebsiteElement> elementObservableList;
    private ObservableList<Website> websiteObservableList = FXCollections.observableList(new ArrayList<>());

    private boolean inlineValidation = false;

    @FXML
    private void initialize() {
        elementObservableList = scrapingTabManager.initWebsiteElementList(elementList);
        elementList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldWs, newWs) -> loadSpecificWebsite(newWs));

        reloadWebsiteList();
        websiteChoiceBox.setItems(websiteObservableList);

        urlField.textProperty().addListener(x -> emptyValidator(urlField));

        websiteChoiceBox.getSelectionModel().selectedItemProperty().addListener(x -> nullValidator(websiteChoiceBox));
    }

    @FXML
    private void handleNewElementButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/scraping/controller/element/newElementPopup.fxml",
                "Neues Element anlegen",
                elementList,
                true, newElementPopupController);
    }

    @FXML
    private void handleDeleteElementButton() {
        WebsiteElement element = elementList.getSelectionModel().getSelectedItem();

        if(element == null) {
            createAlert("Kein Element zum löschen ausgewählt!",
                    "Wählen Sie ein Element aus der Liste aus um dieses zu löschen.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Einstellungen löschen?");
        alert.setContentText("Bitte bestätigen Sie, dass sie dieses Element löschen möchten.");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        scrapingTabManager.deleteSpecificElement(element);
        clearFields();
        reloadElementList();
        elementList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSaveButton() {
        if(!elementIsSelected()) return;
        inlineValidation = true;
        if(!isValidInput()) return;

        WebsiteElement websiteElement = getSelectedElement();
        websiteElement.setWebsite(websiteChoiceBox.getValue());
        websiteElement.setInformationUrl(urlField.getText());

        switch (websiteElement.getMultiplicityType()) {
            case EINZELWERT -> {
                switch (websiteElement.getContentType()) {
                    case STAMMDATEN, AKTIENKURS -> scrapingTabManager.saveSingleCourseOrStockSettings(websiteElement);
                    case WECHSELKURS -> scrapingTabManager.saveSingleExchangeSettings(websiteElement);
                }
            }
            case TABELLE -> {
                switch (websiteElement.getContentType()) {
                    case STAMMDATEN, AKTIENKURS -> {
                        scrapingTabManager.saveTableCourseOrStockSettings(websiteElement);
                        // TODO Description table
                    }
                }
            }
        }



    }

    @FXML
    private void handleCancelButton() {
        loadSpecificWebsite(getSelectedElement());
    }

    private void loadSpecificWebsite(WebsiteElement staleElement) {
        if (staleElement == null) return;

        inlineValidation = false;

        scrapingTabManager.choiceBoxSetWebsiteElement(websiteChoiceBox, staleElement);

        urlField.setText(staleElement.getInformationUrl());

        switch (staleElement.getContentType()) {
            case AKTIENKURS -> {
                switch (staleElement.getMultiplicityType()) {
                    case EINZELWERT -> loadSingleCourseOrStock();
                    case TABELLE -> loadTableCourse();
                }
            }
            case STAMMDATEN -> {
                switch (staleElement.getMultiplicityType()) {
                    case EINZELWERT -> loadSingleCourseOrStock();
                    case TABELLE -> loadTableStock();
                }
            }
            case WECHSELKURS -> {
                switch (staleElement.getMultiplicityType()) {
                    case EINZELWERT -> loadSingleExchange();
                    case TABELLE -> loadTableExchange();
                }
            }
        }
    }

    public void reloadElementList() {
        elementObservableList.clear();
        elementObservableList.addAll(scrapingTabManager.getElements());
    }

    public void reloadWebsiteList() {
        websiteObservableList.clear();
        websiteObservableList.addAll(scrapingTabManager.getWebsites());
    }

    public void selectElement(WebsiteElement element) {
        elementList.getSelectionModel().select(element);

        // it's not guaranteed to be the last in the list
        // nor is it guaranteed that it's the same object
        // match by id
        for(WebsiteElement wse : elementList.getItems()) {
            if(element.getId() == wse.getId()) elementList.getSelectionModel().select(wse);
        }
    }

    private boolean elementIsSelected() {
        if(getSelectedElement() == null) {
            createAlert("Kein Element ausgewählt!",
                    "Wählen Sie ein Element aus der Liste aus oder" +
                            " erstellen Sie ein neues bevor Sie Speichern.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return false;
        }
        return true;
    }

    public WebsiteElement getSelectedElement() {
        return elementList.getSelectionModel().getSelectedItem();
    }

    private void clearFields() {
        urlField.clear();
        subPane.getChildren().clear();
        websiteChoiceBox.setValue(null);
    }

    private void createAlert(String title, String content, Alert.AlertType type, ButtonType buttonType, boolean wait) {
        Alert alert = new Alert(type, content, buttonType);
        alert.setHeaderText(title);
        if(wait) alert.showAndWait();
    }

    private void loadSingleCourseOrStock() {
        ScrapingTabManager.loadSubMenu(singleCourseOrStockSubController,
                "gui/tabs/scraping/controller/element/singleCourseOrStockSubmenu.fxml", subPane);
    }

    private void loadSingleExchange() {
        ScrapingTabManager.loadSubMenu(singleExchangeSubController,
                "gui/tabs/scraping/controller/element/singleExchangeSubmenu.fxml", subPane);
    }

    private void loadTableStock() {

    }

    private void loadTableCourse() {
        ScrapingTabManager.loadSubMenu(tableCourseSubController,
                "gui/tabs/scraping/controller/element/tableCourseSubmenu.fxml", subPane);
    }

    private void loadTableExchange() {

    }


    private boolean isValidInput() {
        inlineValidation = true;
        boolean valid = emptyValidator(urlField);
        valid &= nullValidator(websiteChoiceBox);
        return valid;
    }

    private boolean emptyValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && !input.getText().isBlank();
        decorateField(input, "Dieses Feld darf nicht leer sein!", isValid);
        return isValid;
    }

    private boolean nullValidator(ChoiceBox<Website> choiceBox) {
        boolean isValid = choiceBox.getValue() != null;
        decorateField(choiceBox, "Es muss eine Auswahl getroffen werden!", isValid);
        return isValid;
    }

    private void decorateField(Control input, String tooltip, boolean isValid) {
        input.getStyleClass().remove("bad-input");
        input.setTooltip(null);

        if(!isValid) {
            if(inlineValidation) {
                input.setTooltip(scrapingTabManager.createTooltip(tooltip));
                input.getStyleClass().add("bad-input");
            }
        }
    }
}
