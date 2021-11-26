package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.NewElementPopupController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleCourseOrStockSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleExchangeSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.TableSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ElementManagerCourseAndExchange;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ElementManagerTable;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ScrapingTabManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@Lazy
public class ScrapingElementsTabController {

    @FXML private ListView<WebsiteElement> elementList;
    @FXML private ChoiceBox<Website> websiteChoiceBox;
    @FXML private TextField urlField;
    @FXML private BorderPane subPane;
    @FXML private BorderPane rightPanelBox;
    @FXML private SplitPane rootNode;

    @Autowired
    private ScrapingTabManager scrapingTabManager;
    @Autowired
    private ElementManagerTable scrapingTableManager;
    @Autowired
    private ElementManagerCourseAndExchange scrapingCourseAndExchangeManager;
    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private NewElementPopupController newElementPopupController;
    @Autowired
    private SingleExchangeSubController singleExchangeSubController;
    @Autowired
    private SingleCourseOrStockSubController singleCourseOrStockSubController;
    @Autowired
    private TableSubController tableSubController;

    private ObservableList<WebsiteElement> elementObservableList;
    private final ObservableList<Website> websiteObservableList = FXCollections.observableArrayList();
    private static final BorderPane noSelectionReplacement = new BorderPane(new Label(
            "Wählen Sie ein Element aus oder erstellen Sie ein neues (unten links)"));

    private boolean inlineValidation = false;

    @FXML
    private void initialize() {
        setRightPanelBoxVisible(false);
        elementObservableList = scrapingTabManager.initWebsiteElementList(elementList);
        elementList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldWs, newWs) -> loadSpecificElement(newWs));

        reloadWebsiteList();
        websiteChoiceBox.setItems(websiteObservableList);

        urlField.textProperty().addListener(x -> emptyValidator(urlField));

        websiteChoiceBox.getSelectionModel().selectedItemProperty().addListener(x -> nullValidator(websiteChoiceBox));

        elementList.getSelectionModel().selectFirst();
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
                    "Wählen Sie ein Element aus der Liste aus um dieses zu löschen."
            );
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Einstellungen löschen?");
        alert.setContentText("Bitte bestätigen Sie, dass sie dieses Element löschen möchten.");
        var window = urlField.getScene().getWindow();
        alert.setX(window.getX()+(window.getWidth()/2)-200);
        alert.setY(window.getY()+(window.getHeight()/2)-200);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        scrapingTabManager.deleteSpecificElement(element);
        clearFields();
        reloadElementList();
        setRightPanelBoxVisible(false);
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
                    case STAMMDATEN, AKTIENKURS -> scrapingCourseAndExchangeManager.saveSingleCourseOrStockSettings(websiteElement);
                    case WECHSELKURS -> scrapingCourseAndExchangeManager.saveSingleExchangeSettings(websiteElement);
                }
            }
            case TABELLE -> scrapingTableManager.saveTableSettings(websiteElement);
        }
    }

    @FXML
    private void handleResetButton() {
        loadSpecificElement(getSelectedElement());
    }

    private void loadSpecificElement(WebsiteElement staleElement) {
        if (staleElement == null) return;

        inlineValidation = false;
        setRightPanelBoxVisible(true);

        scrapingTabManager.resetElementRepresentation(urlField, websiteChoiceBox, staleElement);

        switch (staleElement.getMultiplicityType()) {
            case EINZELWERT -> {
                    switch (staleElement.getContentType()) {
                        case WECHSELKURS -> loadSingleExchange();
                        case STAMMDATEN, AKTIENKURS -> loadSingleCourseOrStock();
                    }
                }
            case TABELLE -> loadTable();
        }
    }

    public void reloadElementList() {
        elementObservableList.clear();
        elementObservableList.addAll(scrapingTabManager.getElements());
    }

    private void reloadWebsiteList() {
        websiteObservableList.clear();
        websiteObservableList.addAll(scrapingTabManager.getWebsites());
    }

    public void refresh() {
        WebsiteElement selected = getSelectedElement();
        reloadWebsiteList();
        reloadElementList();

        if(selected != null && elementList.getItems().contains(selected)) {
            scrapingTabManager.resetElementRepresentation(urlField, websiteChoiceBox, selected);
            elementList.getSelectionModel().select(selected);
        } else {
            elementList.getSelectionModel().selectFirst();
        }
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
                            " erstellen Sie ein neues bevor Sie Speichern."
            );
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

    private void createAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setHeaderText(title);
        var window = urlField.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
        alert.showAndWait();
    }

    private void loadSingleCourseOrStock() {
        ScrapingTabManager.loadSubMenu(singleCourseOrStockSubController,
                "gui/tabs/scraping/controller/element/singleCourseOrStockSubmenu.fxml", subPane);
    }

    private void loadSingleExchange() {
        ScrapingTabManager.loadSubMenu(singleExchangeSubController,
                "gui/tabs/scraping/controller/element/singleExchangeSubmenu.fxml", subPane);
    }

    private void loadTable() {
        ScrapingTabManager.loadSubMenu(tableSubController,
                "gui/tabs/scraping/controller/element/tableSubmenu.fxml", subPane);
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
                input.setTooltip(PrimaryTabManagement.createTooltip(tooltip));
                input.getStyleClass().add("bad-input");
            }
        }
    }

    private void setRightPanelBoxVisible(boolean visible) {
        if(!visible) {
            rootNode.getItems().remove(rightPanelBox);
            rootNode.getItems().add(noSelectionReplacement);
        } else {
            if(!rootNode.getItems().contains(rightPanelBox)) {
                rootNode.getItems().remove(noSelectionReplacement);
                rootNode.getItems().add(rightPanelBox);
                rootNode.setDividerPosition(0, 0);
            }
        }
    }
}
