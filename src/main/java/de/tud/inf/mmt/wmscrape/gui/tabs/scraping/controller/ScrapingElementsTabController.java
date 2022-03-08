package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.HistoricWebsiteElementTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.NewElementPopupController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleCourseOrStockSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleExchangeSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.TableSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.*;
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
    private WebsiteManager websiteManager;
    @Autowired
    private ElementManagerTable scrapingTableManager;
    @Autowired
    private ElementManagerCourseAndExchange scrapingCourseAndExchangeManager;
    @Autowired
    private NewElementPopupController newElementPopupController;
    @Autowired
    private SingleExchangeSubController singleExchangeSubController;
    @Autowired
    private SingleCourseOrStockSubController singleCourseOrStockSubController;
    @Autowired
    private TableSubController tableSubController;
    @Autowired
    private HistoricWebsiteElementTabController historicWebsiteElementTabController;

    private ObservableList<WebsiteElement> elementObservableList;
    private final ObservableList<Website> websiteObservableList = FXCollections.observableArrayList();
    private static final BorderPane noSelectionReplacement = new BorderPane(new Label(
            "Wählen Sie eine Konfiguration aus oder erstellen Sie eine neue (unten links)"));

    private boolean inlineValidation = false;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() {
        setRightPanelBoxVisible(false);
        elementObservableList = scrapingTableManager.initWebsiteElementList(elementList, false);
        elementList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldWs, newWs) -> loadSpecificElement(newWs));

        reloadWebsiteList();
        websiteChoiceBox.setItems(websiteObservableList);

        urlField.textProperty().addListener(x -> emptyValidator(urlField));

        websiteChoiceBox.getSelectionModel().selectedItemProperty().addListener(x -> nullValidator(websiteChoiceBox));

        elementList.getSelectionModel().selectFirst();
    }

    /**
     * opens the new element config popup
     */
    @FXML
    private void handleNewElementButton() {
        PrimaryTabManager.loadFxml(
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

        scrapingTableManager.deleteSpecificElement(element);
        clearFields();
        reloadElementList();
        setRightPanelBoxVisible(false);
        elementList.getSelectionModel().selectFirst();

        historicWebsiteElementTabController.refresh();
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

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Die Elementeinstellungen wurden gespeichert.",
                ButtonType.OK);
        alert.setHeaderText("Daten gespeichert!");
        PrimaryTabManager.setAlertPosition(alert , urlField);
        alert.showAndWait();

        historicWebsiteElementTabController.refresh();
    }

    /**
     * resets the input fields by reloading the configuration
     */
    @FXML
    private void handleResetButton() {
        loadSpecificElement(getSelectedElement());
    }

    /**
     * called when selecting a configuration inside the selection list
     * @param staleElement the element from the list (stale because it has no proxy session attached)
     */
    private void loadSpecificElement(WebsiteElement staleElement) {
        if (staleElement == null) return;

        inlineValidation = false;
        setRightPanelBoxVisible(true);

        scrapingTableManager.resetElementRepresentation(urlField, websiteChoiceBox, staleElement);

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
        elementObservableList.addAll(scrapingTableManager.getElements(false));
    }

    private void reloadWebsiteList() {
        websiteObservableList.clear();
        websiteObservableList.addAll(websiteManager.getWebsites(false));
    }

    /**
     * called from other controllers when relevant data changes and the view has to be refreshed
     */
    public void refresh() {
        WebsiteElement selected = getSelectedElement();
        reloadWebsiteList();
        reloadElementList();

        if(selected != null && elementList.getItems().contains(selected)) {
            scrapingTableManager.resetElementRepresentation(urlField, websiteChoiceBox, selected);
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
        ElementManager.loadSubMenu(singleCourseOrStockSubController,
                "gui/tabs/scraping/controller/element/singleCourseOrStockSubmenu.fxml", subPane);
    }

    private void loadSingleExchange() {
        ElementManager.loadSubMenu(singleExchangeSubController,
                "gui/tabs/scraping/controller/element/singleExchangeSubmenu.fxml", subPane);
    }

    private void loadTable() {
        ElementManager.loadSubMenu(tableSubController,
                "gui/tabs/scraping/controller/element/tableSubmenu.fxml", subPane);
    }

    private boolean isValidInput() {
        inlineValidation = true;
        boolean valid = emptyValidator(urlField) && urlValidator(urlField);
        valid &= nullValidator(websiteChoiceBox);
        return valid;
    }

    private boolean emptyValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && !input.getText().isBlank();
        PrimaryTabManager.decorateField(input, "Dieses Feld darf nicht leer sein!", isValid, inlineValidation);
        return isValid;
    }

    private boolean nullValidator(ChoiceBox<Website> choiceBox) {
        boolean isValid = choiceBox.getValue() != null;
        PrimaryTabManager.decorateField(choiceBox, "Es muss eine Auswahl getroffen werden!", isValid,
                                        inlineValidation);
        return isValid;
    }

    private boolean urlValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = value.matches("^(https?://.+)$");
        PrimaryTabManager.decorateField(input, "Die URL muss mit http:// oder https:// beginnen!", isValid,
                                        inlineValidation);
        return isValid;
    }

    /**
     * hides the usual input mask if no configuration exists
     *
     * @param visible if true the normal input mask for a configuration is shown
     */
    private void setRightPanelBoxVisible(boolean visible) {
        if(!visible) {
            rootNode.getItems().remove(rightPanelBox);
            rootNode.getItems().add(noSelectionReplacement);
        } else {
            if(!rootNode.getItems().contains(rightPanelBox)) {
                rootNode.getItems().remove(noSelectionReplacement);
                rootNode.getItems().add(rightPanelBox);
                rootNode.setDividerPosition(0, 0.15);
            }
        }
    }
}
