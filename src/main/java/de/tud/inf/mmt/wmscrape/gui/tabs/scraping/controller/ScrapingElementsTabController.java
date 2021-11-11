package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteElement;
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
    private SingleStockSubController singleStockSubController;
    @Autowired
    private TableExchangeSubController tableExchangeSubController;
    @Autowired
    private TableStockSubController tableStockSubController;

    private ObservableList<WebsiteElement> elementObservableList;
    private ObservableList<Website> websiteObservableList = FXCollections.observableList(new ArrayList<>());


    @FXML
    private void initialize() {
        elementObservableList = scrapingTabManager.initWebsiteElementList(elementList);
        elementList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldWs, newWs) -> loadSpecificWebsite(newWs));

        reloadWebsiteList();
        websiteChoiceBox.setItems(websiteObservableList);
    }

    @FXML
    private void handleNewElementButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/scraping/controller/newElementPopup.fxml",
                "Neues Element anlegen",
                elementList,
                true, newElementPopupController);
    }

    @FXML
    private void handleDeleteElementButton() {
        clearFields();
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
        reloadElementList();
        elementList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSaveButton() {

        WebsiteElement websiteElement = getSelectedElement();
        websiteElement.setWebsite(websiteChoiceBox.getValue());
        websiteElement.setInformationUrl(urlField.getText());

        scrapingTabManager.saveWebsiteElementSettings(websiteElement);

    }

    @FXML
    private void handleCancelButton() {

    }

    private void loadSpecificWebsite(WebsiteElement staleElement) {
        if (staleElement == null) return;

        scrapingTabManager.choiceBoxSetWebsiteElement(websiteChoiceBox, staleElement.getId());

        urlField.setText(staleElement.getInformationUrl());

        switch (staleElement.getContentType()) {
            case AKTIEN -> {
                switch (staleElement.getMultiplicityType()) {
                    case EINZELWERT -> loadSingleStock();
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

    private void loadSingleStock() {
        ScrapingTabManager.loadSubMenu(singleStockSubController,
                "gui/tabs/scraping/controller/singleStockSubmenu.fxml", subPane);
    }

    private void loadTableStock() {

    }

    private void loadSingleExchange() {

    }

    private void loadTableExchange() {

    }
}
