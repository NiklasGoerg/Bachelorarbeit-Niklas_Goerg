package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.website.NewWebsitePopupController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.website.WebsiteTestPopupController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.ScrapingTabManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.Optional;

import static de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes.*;


@Controller
@Lazy
public class ScrapingWebsiteTabController {

    @FXML private ListView<Website> websiteList;

    @FXML private TextField urlField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<IdentType> usernameIdentChoiceBox;
    @FXML private TextField usernameIdentField;
    @FXML private ChoiceBox<IdentType> passwordIdentChoiceBox;
    @FXML private TextField passwordIdentField;
    @FXML private ChoiceBox<IdentType> loginIdentChoiceBox;
    @FXML private TextField loginIdentField;
    @FXML private ChoiceBox<IdentType> logoutIdentChoiceBox;
    @FXML private TextField logoutIdentField;
    @FXML private ChoiceBox<IdentType> cookieAcceptIdentChoiceBox;
    @FXML private TextField cookieAcceptIdentField;
    @FXML private VBox rightPanelBox;
    @FXML private SplitPane rootNode;

    private ObservableList<Website> websiteObservableList;
    private boolean inlineValidation = false;
    private static final BorderPane noSelectionReplacement = new BorderPane(new Label(
            "Wählen Sie eine Konfiguration aus oder erstellen Sie eine neue (unten links)"));

    @Autowired
    private ScrapingTabManager scrapingTabManager;
    @Autowired
    private PrimaryTabManagement primaryTabManagement;
    @Autowired
    private NewWebsitePopupController newWebsitePopupController;
    @Autowired
    private WebsiteTestPopupController websiteTestPopupController;

    @FXML
    private void initialize() {

        setRightPanelBoxVisible(false);
        websiteObservableList = scrapingTabManager.initWebsiteList(websiteList);
        websiteList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldWs, newWs) -> loadSpecificWebsite(newWs));

        // add listener for inline validation
        urlField.textProperty().addListener((o,ov,nv) -> emptyValidator(urlField));
        usernameField.textProperty().addListener((o,ov,nv) -> emptyValidator(usernameField));
        passwordField.textProperty().addListener((o,ov,nv) -> emptyValidator(passwordField));
        usernameIdentField.textProperty().addListener((o,ov,nv) -> validUsernameIdentField());
        passwordIdentField.textProperty().addListener((o,ov,nv) -> validPasswordIdentField());
        loginIdentField.textProperty().addListener((o,ov,nv) -> validLoginIdentField());
        logoutIdentField.textProperty().addListener((o,ov,nv) -> escapeValidator(logoutIdentField));
        cookieAcceptIdentField.textProperty().addListener((o,ov,nv) -> escapeValidator(cookieAcceptIdentField));

        // set choicebox options
        addTypeToChoiceBox(usernameIdentChoiceBox, IDENT_TYPE_DEACTIVATED);
        addTypeToChoiceBox(passwordIdentChoiceBox, IDENT_TYPE_DEACTIVATED);
        addTypeToChoiceBox(loginIdentChoiceBox, IDENT_TYPE_DEACTIVATED_ENTER);
        addTypeToChoiceBox(logoutIdentChoiceBox, IDENT_TYPE_DEACTIVATED_URL);
        addTypeToChoiceBox(cookieAcceptIdentChoiceBox, IDENT_TYPE_DEACTIVATED);

        addDeselectListener(usernameIdentChoiceBox);
        addDeselectListener(passwordIdentChoiceBox);

        loginIdentChoiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            loginIdentField.setEditable(nv != IdentType.ENTER);
            loginIdentField.setText("-");
        });



        websiteList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleNewWebsiteButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/scraping/controller/website/newWebsitePopup.fxml",
                "Neue Webseite anlegen",
                websiteList,
                true, newWebsitePopupController);
    }

    @FXML
    private void handleDeleteWebsiteButton() {
        Website website = getSelectedWebsite();

        if(website == null) {
            createAlert("Keine Webseite zum löschen ausgewählt!",
                    "Wählen Sie eine Webseite aus der Liste aus um diese zu löschen."
            );
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Einstellungen löschen?");
        alert.setContentText("Bitte bestätigen Sie, dass sie diese Webseitenkonfiguration löschen möchten.");
        setAlertPosition(alert);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        scrapingTabManager.deleteSpecificWebsite(website);
        clearFields();
        reloadWebsiteList();
        setRightPanelBoxVisible(false);
        websiteList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSaveButton() {

        if(noWebsiteSelected()) return;
        inlineValidation = true;
        if(!isValidInput()) return;


        Website website = websiteList.getSelectionModel().getSelectedItem();

        setFieldDataToWebsite(website);

        scrapingTabManager.saveWebsite(website);

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Die Webseitenkonfiguration wurde gespeichert.",
                ButtonType.OK);
        alert.setHeaderText("Daten gespeichert!");
        setAlertPosition(alert);
        alert.showAndWait();
    }

    @FXML
    private void handleResetButton() {
        loadSpecificWebsite(getSelectedWebsite());
    }

    @FXML
    private void handleTestButton() {
        if(noWebsiteSelected()) return;
        inlineValidation = true;
        if(!isValidInput() || urlField.getText().equals("-")) return;

        primaryTabManagement.loadFxml(
                "gui/tabs/scraping/controller/website/websiteTestPopup.fxml",
                "Login Test",
                websiteList,
                true, websiteTestPopupController);
    }

    private boolean noWebsiteSelected() {
        if(getSelectedWebsite() == null) {
            createAlert("Keine Webseite ausgewählt!",
                    "Wählen Sie eine Webseite aus der Liste aus oder" +
                            " erstellen Sie eine neue bevor Sie Speichern."
            );
            return true;
        }
        return false;
    }

    public Website getSelectedWebsite() {
        return websiteList.getSelectionModel().getSelectedItem();
    }

    private void clearFields() {
        clearTopFields();

        cookieAcceptIdentField.clear();
        usernameIdentChoiceBox.setValue(null);
        passwordIdentChoiceBox.setValue(null);
        loginIdentChoiceBox.setValue(null);
        logoutIdentChoiceBox.setValue(null);
        cookieAcceptIdentChoiceBox.setValue(null);
    }

    private void clearTopFields() {
        urlField.clear();
        usernameField.clear();
        passwordField.clear();
        usernameIdentField.clear();
        passwordIdentField.clear();
        loginIdentField.clear();
        logoutIdentField.clear();
    }

    private void setFieldDataToWebsite(Website website) {
        website.setUrl(urlField.getText());
        website.setUsername(usernameField.getText());
        website.setPassword(passwordField.getText());
        website.setUsernameIdentType(usernameIdentChoiceBox.getValue());
        website.setUsernameIdent(usernameIdentField.getText());
        website.setPasswordIdentType(passwordIdentChoiceBox.getValue());
        website.setPasswordIdent(passwordIdentField.getText());
        website.setLoginButtonIdentType(loginIdentChoiceBox.getValue());
        website.setLoginButtonIdent(loginIdentField.getText());
        website.setLogoutIdentType(logoutIdentChoiceBox.getValue());
        website.setLogoutIdent(logoutIdentField.getText());
        website.setCookieAcceptIdentType(cookieAcceptIdentChoiceBox.getValue());
        website.setCookieAcceptIdent(cookieAcceptIdentField.getText());
    }

    public void selectWebsite(Website website) {
        websiteList.getSelectionModel().select(website);

        // it's not guaranteed to be the last in the list
        // nor is it guaranteed that it's the same object
        // match by id
        for(Website ws : websiteList.getItems()) {
            if(website.getId() == ws.getId()) websiteList.getSelectionModel().select(ws);
        }
    }

    public void reloadWebsiteList() {
        websiteObservableList.clear();
        websiteObservableList.addAll(scrapingTabManager.getWebsites());
    }

    private void loadSpecificWebsite(Website website) {
        if (website == null) return;

        inlineValidation = false;

        setRightPanelBoxVisible(true);

        deselectOnDeactivated(passwordIdentChoiceBox.getValue(), website.getPasswordIdentType());

        urlField.setText(website.getUrl());
        usernameField.setText(website.getUsername());
        passwordField.setText(website.getPassword());
        usernameIdentChoiceBox.setValue(website.getUsernameIdentType());
        usernameIdentField.setText(website.getUsernameIdent());
        passwordIdentChoiceBox.setValue(website.getPasswordIdentType());
        passwordIdentField.setText(website.getPasswordIdent());
        loginIdentChoiceBox.setValue(website.getLoginButtonIdentType());
        loginIdentField.setText(website.getLoginButtonIdent());
        logoutIdentChoiceBox.setValue(website.getLogoutIdentType());
        logoutIdentField.setText(website.getLogoutIdent());
        cookieAcceptIdentChoiceBox.setValue(website.getCookieAcceptIdentType());
        cookieAcceptIdentField.setText(website.getCookieAcceptIdent());

        // just here to remove eventually existing error style attributes
        isValidInput();
    }

    private void createAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setHeaderText(title);
        setAlertPosition(alert);
        alert.showAndWait();
    }

    private boolean isValidInput() {
        // evaluate all to highlight all
        boolean valid = validUrlField();
        valid &= emptyValidator(usernameField);
        valid &= emptyValidator(passwordField);
        valid &= validUsernameIdentField();
        valid &= validPasswordIdentField();
        valid &= validLoginIdentField();
        valid &= escapeValidator(logoutIdentField);
        valid &= escapeValidator(cookieAcceptIdentField);
        return valid;
    }

    private boolean validUrlField() {
        return emptyValidator(urlField) && urlValidator(urlField);
    }

    private boolean validUsernameIdentField() {
        return emptyValidator(usernameIdentField) && escapeValidator(usernameIdentField);
    }

    private boolean validPasswordIdentField() {
        return emptyValidator(passwordIdentField) && escapeValidator(passwordIdentField);
    }

    private boolean validLoginIdentField() {
        return emptyValidator(loginIdentField) && escapeValidator(loginIdentField);
    }

    private boolean emptyValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && !input.getText().isBlank();
        decorateField(input, "Dieses Feld darf nicht leer sein!", isValid);
        return isValid;
    }

    private boolean urlValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = value.matches("^(https?://.+|-)$"); // "-" to allow saving with deactivated fields
        decorateField(input, "Die URL muss mit http:// oder https:// beginnen!", isValid);
        return isValid;
    }

    private boolean escapeValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = !value.matches("^.*[\"´`]+.*$");
        decorateField(input, "Die Symbole \",´,` sind nicht erlaubt!", isValid);
        return isValid;
    }

    private void decorateField(TextInputControl input, String tooltip, boolean isValid) {
        input.getStyleClass().remove("bad-input");
        input.setTooltip(null);

        if(!isValid) {
            if(inlineValidation) {
                input.setTooltip(PrimaryTabManagement.createTooltip(tooltip));
                input.getStyleClass().add("bad-input");
            }
        }
    }

    private void addDeselectListener(ChoiceBox<IdentType> choiceBox) {
        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> deselectOnDeactivated(ov, nv));
    }

    private void addTypeToChoiceBox(ChoiceBox<IdentType> choiceBox, IdentType[] identType) {
        choiceBox.getItems().addAll(identType);
    }

    private void deselectOnDeactivated(IdentType oldIdentType,IdentType newIdentType) {
        if(newIdentType == IdentType.DEAKTIVIERT) {
            usernameIdentChoiceBox.setValue(IdentType.DEAKTIVIERT);
            passwordIdentChoiceBox.setValue(IdentType.DEAKTIVIERT);
            loginIdentChoiceBox.setValue(IdentType.ENTER);
            logoutIdentChoiceBox.setValue(IdentType.DEAKTIVIERT);

            logoutIdentChoiceBox.getItems().clear();
            logoutIdentChoiceBox.getItems().add(IdentType.DEAKTIVIERT);
            logoutIdentChoiceBox.setValue(IdentType.DEAKTIVIERT);

            urlField.setText("-");
            usernameField.setText("-");
            passwordField.setText("-");
            usernameIdentField.setText("-");
            passwordIdentField.setText("-");
            loginIdentField.setText("-");

            setEditable(false);
        } else if (oldIdentType == IdentType.DEAKTIVIERT) {
            usernameIdentChoiceBox.setValue(newIdentType);
            passwordIdentChoiceBox.setValue(newIdentType);

            logoutIdentChoiceBox.getItems().clear();
            logoutIdentChoiceBox.getItems().addAll(IDENT_TYPE_DEACTIVATED_URL);
            logoutIdentChoiceBox.setValue(IdentType.ID);

            clearTopFields();
            setEditable(true);
        } else {
            setEditable(true);
        }
    }


    private void setEditable(boolean editable) {
        urlField.setEditable(editable);
        usernameField.setEditable(editable);
        passwordField.setEditable(editable);
        usernameIdentField.setEditable(editable);
        passwordIdentField.setEditable(editable);
        loginIdentField.setEditable(editable);
        logoutIdentField.setEditable(editable);
    }

    public Website getWebsiteUnpersistedData() {
        Website newWebsite = new Website("test");
        setFieldDataToWebsite(newWebsite);
        return newWebsite;
    }

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

    private void setAlertPosition(Alert alert) {
        var window = urlField.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
    }

}
