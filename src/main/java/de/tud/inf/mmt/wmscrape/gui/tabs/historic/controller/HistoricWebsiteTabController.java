package de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.website.HistoricWebsiteTestPopupController;
import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.website.NewHistoricWebsitePopupController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui.WebsiteManager;
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
public class HistoricWebsiteTabController {

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
    @FXML private ChoiceBox<IdentType> cookieConfigIdentChoiceBox;
    @FXML private TextField cookieConfigIdentField;
    @FXML private VBox rightPanelBox;
    @FXML private SplitPane rootNode;

    @FXML private ChoiceBox<IdentType> notificationDeclineIdentChoiceBox;
    @FXML private TextField notificationDeclineIdentField;

    @FXML private TextField informationUrlField;

    @FXML private ChoiceBox<IdentType> searchIdentChoiceBox;
    @FXML private TextField searchIdentField;
    @FXML private ChoiceBox<IdentType> searchButtonIdentChoiceBox;
    @FXML private TextField searchButtonIdentField;
    @FXML private ChoiceBox<IdentType> historicLinkIdentChoiceBox;
    @FXML private TextField historicLinkIdentField;

    @FXML private ChoiceBox<IdentType> dateFromDayIdentChoiceBox;
    @FXML private TextField dateFromDayIdentField;
    @FXML private ChoiceBox<IdentType> dateFromMonthIdentChoiceBox;
    @FXML private TextField dateFromMonthIdentField;
    @FXML private ChoiceBox<IdentType> dateFromYearIdentChoiceBox;
    @FXML private TextField dateFromYearIdentField;

    @FXML private TextField dateFromField;
    @FXML private TextField dateUntilField;

    @FXML private ChoiceBox<IdentType> dateUntilDayIdentChoiceBox;
    @FXML private TextField dateUntilDayIdentField;
    @FXML private ChoiceBox<IdentType> dateUntilMonthIdentChoiceBox;
    @FXML private TextField dateUntilMonthIdentField;
    @FXML private ChoiceBox<IdentType> dateUntilYearIdentChoiceBox;
    @FXML private TextField dateUntilYearIdentField;

    @FXML private ChoiceBox<IdentType> loadButtonIdentChoiceBox;
    @FXML private TextField loadButtonIdentField;


    private ObservableList<Website> websiteObservableList;
    private boolean inlineValidation = false;
    private static final BorderPane noSelectionReplacement = new BorderPane(new Label(
            "Wählen Sie eine Konfiguration aus oder erstellen Sie eine neue (unten links)"));

    @Autowired
    private WebsiteManager websiteManager;
    @Autowired
    private NewHistoricWebsitePopupController newHistoricWebsitePopupController;
    @Autowired
    private HistoricWebsiteTestPopupController historicWebsiteTestPopupController;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() {

        setRightPanelBoxVisible(false);
        websiteObservableList = websiteManager.initWebsiteList(websiteList, true);
        websiteList.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldWs, newWs) -> loadSpecificWebsite(newWs));

        // add listener for inline validation
        urlField.textProperty().addListener((o,ov,nv) -> emptyValidator(urlField));
        usernameField.textProperty().addListener((o,ov,nv) -> emptyValidator(usernameField));
        passwordField.textProperty().addListener((o,ov,nv) -> emptyValidator(passwordField));
        usernameIdentField.textProperty().addListener((o,ov,nv) -> validIdentField(usernameIdentField, true));
        passwordIdentField.textProperty().addListener((o,ov,nv) -> validIdentField(passwordIdentField, true));
        loginIdentField.textProperty().addListener((o,ov,nv) -> validIdentField(loginIdentField, true));
        logoutIdentField.textProperty().addListener((o,ov,nv) -> escapeValidator(logoutIdentField));
        cookieConfigIdentField.textProperty().addListener((o, ov, nv) -> escapeValidator(cookieConfigIdentField));
        notificationDeclineIdentField.textProperty().addListener((o,ov,nv) -> escapeValidator(notificationDeclineIdentField));

        informationUrlField.textProperty().addListener((o, ov, nv) -> validUrlField(informationUrlField));
        searchIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(searchIdentField, false));
        searchButtonIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(searchButtonIdentField, false));
        historicLinkIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(historicLinkIdentField, false));

        dateFromDayIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(dateFromDayIdentField, false));
        dateFromMonthIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(dateFromMonthIdentField, true));
        dateFromYearIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(dateFromYearIdentField, true));

        dateUntilDayIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(dateUntilDayIdentField, false));
        dateUntilMonthIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(dateUntilMonthIdentField, true));
        dateUntilYearIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(dateUntilYearIdentField, true));

        loadButtonIdentField.textProperty().addListener((o, ov, nv) -> validIdentField(loadButtonIdentField, false));

        // set choicebox options
        addTypeToChoiceBox(usernameIdentChoiceBox, IDENT_TYPE_DEACTIVATED);
        addTypeToChoiceBox(passwordIdentChoiceBox, IDENT_TYPE_DEACTIVATED);
        addTypeToChoiceBox(loginIdentChoiceBox, IDENT_TYPE_DEACTIVATED_ENTER);
        addTypeToChoiceBox(logoutIdentChoiceBox, IDENT_TYPE_DEACTIVATED_URL);
        addTypeToChoiceBox(cookieConfigIdentChoiceBox, IDENT_TYPE_DEACTIVATED);
        addTypeToChoiceBox(notificationDeclineIdentChoiceBox, IDENT_TYPE_DEACTIVATED);

        addTypeToChoiceBox(searchButtonIdentChoiceBox, IDENT_TYPE_DEACTIVATED_ENTER);
        addTypeToChoiceBox(searchIdentChoiceBox, IDENT_TYPE_DEACTIVATED_ENTER);
        addTypeToChoiceBox(historicLinkIdentChoiceBox, IDENT_TYPE_DEACTIVATED);

        addTypeToChoiceBox(dateFromDayIdentChoiceBox, IDENT_TYPE_SIMPLE);
        addTypeToChoiceBox(dateFromMonthIdentChoiceBox, IDENT_TYPE_SIMPLE);
        addTypeToChoiceBox(dateFromYearIdentChoiceBox, IDENT_TYPE_SIMPLE);
        addTypeToChoiceBox(dateUntilDayIdentChoiceBox, IDENT_TYPE_SIMPLE);
        addTypeToChoiceBox(dateUntilMonthIdentChoiceBox, IDENT_TYPE_SIMPLE);
        addTypeToChoiceBox(dateUntilYearIdentChoiceBox, IDENT_TYPE_SIMPLE);

        addTypeToChoiceBox(loadButtonIdentChoiceBox, IDENT_TYPE_DEACTIVATED);

        addDeselectListener(usernameIdentChoiceBox);
        addDeselectListener(passwordIdentChoiceBox);

        loginIdentChoiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            loginIdentField.setEditable(nv != IdentType.ENTER);
            if(nv == IdentType.ENTER) loginIdentField.setText("-");
        });



        websiteList.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleNewWebsiteButton() {
        PrimaryTabManager.loadFxml(
                "gui/tabs/scraping/controller/website/newWebsitePopup.fxml",
                "Neue Webseite anlegen",
                websiteList,
                true, newHistoricWebsitePopupController, false);
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
        PrimaryTabManager.setAlertPosition(alert , urlField);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        websiteManager.deleteSpecificWebsite(website);
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

        websiteManager.saveWebsite(website);

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "Die Webseitenkonfiguration wurde gespeichert.",
                ButtonType.OK);
        alert.setHeaderText("Daten gespeichert!");
        PrimaryTabManager.setAlertPosition(alert , urlField);
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
//        if(!isValidInput() || urlField.getText().equals("-")) return;

        PrimaryTabManager.loadFxml(
                "gui/tabs/scraping/controller/website/websiteTestPopup.fxml",
                "Login Test",
                websiteList,
                true, historicWebsiteTestPopupController, false);
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

        cookieConfigIdentField.clear();
        usernameIdentChoiceBox.setValue(null);
        passwordIdentChoiceBox.setValue(null);
        loginIdentChoiceBox.setValue(null);
        logoutIdentChoiceBox.setValue(null);
        cookieConfigIdentChoiceBox.setValue(null);

        notificationDeclineIdentField.clear();
        notificationDeclineIdentChoiceBox.setValue(null);

        informationUrlField.clear();

        searchIdentField.clear();
        searchIdentChoiceBox.setValue(null);

        searchButtonIdentField.clear();
        searchButtonIdentChoiceBox.setValue(null);

        historicLinkIdentField.clear();
        historicLinkIdentChoiceBox.setValue(null);

        dateFromDayIdentField.clear();
        dateFromMonthIdentField.clear();
        dateFromYearIdentField.clear();
        dateFromDayIdentChoiceBox.setValue(null);
        dateFromMonthIdentChoiceBox.setValue(null);
        dateFromYearIdentChoiceBox.setValue(null);

        dateUntilDayIdentField.clear();
        dateUntilMonthIdentField.clear();
        dateUntilYearIdentField.clear();

        dateFromField.clear();
        dateUntilField.clear();

        dateUntilDayIdentChoiceBox.setValue(null);
        dateUntilMonthIdentChoiceBox.setValue(null);
        dateUntilYearIdentChoiceBox.setValue(null);

        loadButtonIdentField.clear();
        loadButtonIdentChoiceBox.setValue(null);
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
        website.setLoginUrl(urlField.getText());
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
        website.setCookieAcceptIdentType(cookieConfigIdentChoiceBox.getValue());
        website.setCookieAcceptIdent(cookieConfigIdentField.getText());
        website.setDeclineNotificationIdentType(notificationDeclineIdentChoiceBox.getValue());
        website.setNotificationDeclineIdent(notificationDeclineIdentField.getText());

        website.setHistoric(true);

        website.setSearchUrl(informationUrlField.getText());
        website.setSearchFieldIdentType(searchIdentChoiceBox.getValue());
        website.setSearchButtonIdent(searchButtonIdentField.getText());
        website.setSearchButtonIdentType(searchButtonIdentChoiceBox.getValue());
        website.setSearchFieldIdent(searchIdentField.getText());
        website.setHistoricLinkIdentType(historicLinkIdentChoiceBox.getValue());
        website.setHistoricLinkIdent(historicLinkIdentField.getText());
        website.setDateFromDayIdentType(dateFromDayIdentChoiceBox.getValue());
        website.setDateFromDayIdent(dateFromDayIdentField.getText());
        website.setDateFromMonthIdentType(dateFromMonthIdentChoiceBox.getValue());
        website.setDateFromMonthIdent(dateFromMonthIdentField.getText());
        website.setDateFromYearIdentType(dateFromYearIdentChoiceBox.getValue());
        website.setDateFromYearIdent(dateFromYearIdentField.getText());

        website.setDateFrom(dateFromField.getText());
        website.setDateUntil(dateUntilField.getText());

        website.setDateUntilDayIdentType(dateUntilDayIdentChoiceBox.getValue());
        website.setDateUntilDayIdent(dateUntilDayIdentField.getText());
        website.setDateUntilMonthIdentType(dateUntilMonthIdentChoiceBox.getValue());
        website.setDateUntilMonthIdent(dateUntilMonthIdentField.getText());
        website.setDateUntilYearIdentType(dateUntilYearIdentChoiceBox.getValue());
        website.setDateUntilYearIdent(dateUntilYearIdentField.getText());
        website.setLoadButtonIdentType(loadButtonIdentChoiceBox.getValue());
        website.setLoadButtonIdent(loadButtonIdentField.getText());
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
        websiteObservableList.addAll(websiteManager.getWebsites(true));
    }

    private void loadSpecificWebsite(Website website) {
        if (website == null) return;

        inlineValidation = false;

        setRightPanelBoxVisible(true);

        deselectOnDeactivated(passwordIdentChoiceBox.getValue(), website.getPasswordIdentType());

        urlField.setText(website.getLoginUrl());
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
        cookieConfigIdentChoiceBox.setValue(website.getCookieAcceptIdentType());
        cookieConfigIdentField.setText(website.getCookieAcceptIdent());

        notificationDeclineIdentChoiceBox.setValue(website.getDeclineNotificationIdentType());
        notificationDeclineIdentField.setText(website.getNotificationDeclineIdent());
        informationUrlField.setText(website.getSearchUrl());
        searchIdentChoiceBox.setValue(website.getSearchFieldIdentType());
        searchIdentField.setText(website.getSearchFieldIdent());
        searchButtonIdentChoiceBox.setValue(website.getSearchButtonIdentType());
        searchButtonIdentField.setText(website.getSearchButtonIdent());
        historicLinkIdentChoiceBox.setValue(website.getHistoricLinkIdentType());
        historicLinkIdentField.setText(website.getHistoricLinkIdent());
        dateFromDayIdentChoiceBox.setValue(website.getDateFromDayIdentType());
        dateFromMonthIdentChoiceBox.setValue(website.getDateFromMonthIdentType());
        dateFromYearIdentChoiceBox.setValue(website.getDateFromYearIdentType());
        dateFromDayIdentField.setText(website.getDateFromDayIdent());
        dateFromMonthIdentField.setText(website.getDateFromMonthIdent());
        dateFromYearIdentField.setText(website.getDateFromYearIdent());
        dateFromField.setText(website.getDateFrom());
        dateUntilField.setText(website.getDateUntil());
        dateUntilDayIdentChoiceBox.setValue(website.getDateUntilDayIdentType());
        dateUntilMonthIdentChoiceBox.setValue(website.getDateUntilMonthIdentType());
        dateUntilYearIdentChoiceBox.setValue(website.getDateUntilYearIdentType());
        dateUntilDayIdentField.setText(website.getDateUntilDayIdent());
        dateUntilMonthIdentField.setText(website.getDateUntilMonthIdent());
        dateUntilYearIdentField.setText(website.getDateUntilYearIdent());
        loadButtonIdentChoiceBox.setValue(website.getLoadButtonIdentType());
        loadButtonIdentField.setText(website.getLoadButtonIdent());

        // just here to remove eventually existing error style attributes
        isValidInput();
    }

    private void createAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setHeaderText(title);
        PrimaryTabManager.setAlertPosition(alert , urlField);
        alert.showAndWait();
    }

    private boolean isValidInput() {
        // evaluate all to highlight all
        boolean valid = validUrlField(urlField);
        valid &= emptyValidator(usernameField);
        valid &= emptyValidator(passwordField);
        valid &= validIdentField(usernameIdentField, true);
        valid &= validIdentField(passwordIdentField, true);
        valid &= validIdentField(loginIdentField, true);
        valid &= escapeValidator(logoutIdentField);
        valid &= escapeValidator(cookieConfigIdentField);
        valid &= validUrlField(informationUrlField);
        valid &= validIdentField(searchIdentField, false);
        valid &= validIdentField(historicLinkIdentField, false);
        valid &= validIdentField(dateFromDayIdentField, false);
        valid &= validIdentField(dateFromMonthIdentField, true);
        valid &= validIdentField(dateFromYearIdentField, true);
        valid &= validIdentField(dateUntilDayIdentField, false);
        valid &= validIdentField(dateUntilMonthIdentField, true);
        valid &= validIdentField(dateUntilYearIdentField, true);
        valid &= validIdentField(loadButtonIdentField, false);

        return valid;
    }

    private boolean validUrlField(TextField identField) {
        return emptyValidator(identField) && urlValidator(identField);
    }

    private boolean validIdentField(TextField identField, boolean canBeEmpty) {
        return (canBeEmpty || emptyValidator(identField)) && escapeValidator(identField);
    }

    private boolean emptyValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && !input.getText().isBlank();
        PrimaryTabManager.decorateField(input, "Dieses Feld darf nicht leer sein!", isValid, inlineValidation);
        return isValid;
    }

    private boolean urlValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = value.matches("^(https?://.+|-)$"); // "-" to allow saving with deactivated fields
        PrimaryTabManager.decorateField(input, "Die URL muss mit http:// oder https:// beginnen!", isValid,
                inlineValidation);
        return isValid;
    }

    private boolean escapeValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = !value.matches("^.*[\"´`]+.*$");
        PrimaryTabManager.decorateField(input, "Die Symbole \",´,` sind nicht erlaubt!", isValid, inlineValidation);
        return isValid;
    }

    private void addDeselectListener(ChoiceBox<IdentType> choiceBox) {
        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> deselectOnDeactivated(ov, nv));
    }

    private void addTypeToChoiceBox(ChoiceBox<IdentType> choiceBox, IdentType[] identType) {
        choiceBox.getItems().addAll(identType);
    }

    /**
     * if the password or username identification is set to deactivate some fields are no longer editable
     *
     * @param oldIdentType the previous set {@link de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType}
     * @param newIdentType the now set {@link de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType}
     */
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

    /**
     * creates a temporary copy of a website to use the data with the website tester without saving
     * @return the temporary website
     */
    public Website getWebsiteUnpersistedData() {
        Website newWebsite = new Website("test");
        setFieldDataToWebsite(newWebsite);
        return newWebsite;
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
