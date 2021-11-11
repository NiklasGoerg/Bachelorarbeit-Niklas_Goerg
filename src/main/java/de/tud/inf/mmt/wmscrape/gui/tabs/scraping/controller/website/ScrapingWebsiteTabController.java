package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivatedUrl;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class ScrapingWebsiteTabController {

    @FXML private ListView<Website> websiteList;

    @FXML private TextField urlField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ChoiceBox<IdentType> usernameIdentChoiceBox;
    @FXML private TextField usernameIdentField;
    @FXML private ChoiceBox<IdentType> passwordIdentChoiceBox;
    @FXML private TextField passwordIdentField;
    @FXML private ChoiceBox<IdentTypeDeactivated> loginIdentChoiceBox;
    @FXML private TextField loginIdentField;
    @FXML private ChoiceBox<IdentTypeDeactivatedUrl> logoutIdentChoiceBox;
    @FXML private TextField logoutIdentField;
    @FXML private ChoiceBox<IdentTypeDeactivated> cookieAcceptIdentChoiceBox;
    @FXML private TextField cookieAcceptIdentField;
    @FXML private ChoiceBox<IdentTypeDeactivated> cookieHideChoiceBox;
    @FXML private TextField cookieHideIdentField;

    private ObservableList<Website> websiteObservableList;
    private boolean inlineValidation = false;

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
        cookieHideIdentField.textProperty().addListener((o,ov,nv) -> escapeValidator(cookieHideIdentField));

        // set choicebox options
        usernameIdentChoiceBox.getItems().addAll(IdentType.values());
        passwordIdentChoiceBox.getItems().addAll(IdentType.values());
        loginIdentChoiceBox.getItems().addAll(IdentTypeDeactivated.values());
        logoutIdentChoiceBox.getItems().addAll(IdentTypeDeactivatedUrl.values());
        cookieAcceptIdentChoiceBox.getItems().addAll(IdentTypeDeactivated.values());
        cookieHideChoiceBox.getItems().addAll(IdentTypeDeactivated.values());

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
        clearFields();
        Website website = getSelectedWebsite();

        if(website == null) {
            createAlert("Keine Webseite zum löschen ausgewählt!",
                    "Wählen Sie eine Webseite aus der Liste aus um diese zu löschen.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Einstellungen löschen?");
        alert.setContentText("Bitte bestätigen Sie, dass sie diese Webseitenkonfiguration löschen möchten.");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        scrapingTabManager.deleteSpecificWebsite(website);
        reloadWebsiteList();
        websiteList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSaveButton() {

        if(!websiteIsSelected()) return;
        inlineValidation = true;
        if(!isValidInput()) return;


        Website website = websiteList.getSelectionModel().getSelectedItem();

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
        website.setCookieHideIdentType(cookieHideChoiceBox.getValue());
        website.setCookieHideIdent(cookieHideIdentField.getText());

        scrapingTabManager.saveWebsite(website);

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Die Webseitenkonfiguration wurde gespeichert.",
                ButtonType.OK);
        alert.setHeaderText("Daten gespeichert!");
        alert.showAndWait();
    }

    @FXML
    private void handleResetButton() {
        loadSpecificWebsite(getSelectedWebsite());
    }

    @FXML
    private void handleTestButton() {
        primaryTabManagement.loadFxml(
                "gui/tabs/scraping/controller/website/websiteTestPopup.fxml",
                "Login Test",
                websiteList,
                true, websiteTestPopupController);
    }

    private boolean websiteIsSelected() {
        if(getSelectedWebsite() == null) {
            createAlert("Keine Webseite ausgewählt!",
                    "Wählen Sie eine Webseite aus der Liste aus oder" +
                            " erstellen Sie eine neue bevor Sie Speichern.",
                    Alert.AlertType.ERROR, ButtonType.OK, true);
            return false;
        }
        return true;
    }

    public Website getSelectedWebsite() {
        return websiteList.getSelectionModel().getSelectedItem();
    }

    private void clearFields() {
        urlField.clear();
        usernameField.clear();
        passwordField.clear();
        usernameIdentChoiceBox.setValue(null);
        usernameIdentField.clear();
        passwordIdentChoiceBox.setValue(null);
        passwordIdentField.clear();
        loginIdentChoiceBox.setValue(null);
        loginIdentField.clear();
        logoutIdentChoiceBox.setValue(null);
        logoutIdentField.clear();
        cookieAcceptIdentChoiceBox.setValue(null);
        cookieAcceptIdentField.clear();
        cookieHideChoiceBox.setValue(null);
        cookieHideIdentField.clear();
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
        cookieHideChoiceBox.setValue(website.getCookieHideIdentType());
        cookieHideIdentField.setText(website.getCookieHideIdent());

        // just here to remove eventually existing error style attributes
        isValidInput();
    }

    private void createAlert(String title, String content, Alert.AlertType type, ButtonType buttonType, boolean wait) {
        Alert alert = new Alert(type, content, buttonType);
        alert.setHeaderText(title);
        if(wait) alert.showAndWait();
    }


    private boolean isValidInput() {
        // evaluate all to highlight all
        boolean valid = emptyValidator(urlField);
        valid &= emptyValidator(usernameField);
        valid &= emptyValidator(passwordField);
        valid &= validUsernameIdentField();
        valid &= validPasswordIdentField();
        valid &= validLoginIdentField();
        valid &= escapeValidator(logoutIdentField);
        valid &= escapeValidator(cookieAcceptIdentField);
        valid &= escapeValidator(cookieHideIdentField);
        return valid;
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

    private boolean escapeValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = !value.matches("^.*[\\\"´`]+.*$");
        decorateField(input, "Die Symbole \",´,` sind nicht erlaubt!", isValid);
        return isValid;
    }

    private void decorateField(TextInputControl input, String tooltip, boolean isValid) {
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
