package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class ExistingUserLoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    ValidationSupport usernameValidation = new ValidationSupport();
    ValidationSupport passwordValidation = new ValidationSupport();

    @FXML
    private void initialize()  {
        LoginManager.loadUserProperties();
        addValidation();
        usernameField.setText(SpringIndependentData.getUsername());
    }

    @FXML
    private void handleNewUserButton() {
        LoginManager.loadFxml("gui/login/controller/newUserLogin.fxml", "Neuen Nutzer anlegen", usernameField, false);
    }

    @FXML
    private void handleLoginButton() {
        if(!isValidInput()) {
            return;
        }

        boolean success = LoginManager.loginExistingUser(usernameField.getText(), passwordField.getText(), usernameField);

        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Der Verbindungsversuch ist fehlgeschlagen!\n" +
                    "Überprüfen Sie den Pfad sowie Nutzername und Passwort.", ButtonType.OK);
            alert.setHeaderText("Verbindungsfehler");
            alert.setX(usernameField.getScene().getWindow().getX()+(usernameField.getScene().getWindow().getWidth()/2)-200);
            alert.setY(usernameField.getScene().getWindow().getY()+(usernameField.getScene().getWindow().getHeight()/2)-200);
            alert.showAndWait();
        }
    }

    @FXML
    private void handleChangeDbPathButton() {
        LoginManager.loadFxml("gui/login/controller/changeDbPathPopup.fxml", "Datenbankpfad ändern", usernameField, true);
    }

    private void addValidation() {

        Validator<String> emptyName = Validator.createEmptyValidator("Es muss ein Nutzername angegeben werden!");
        Validator<String> illegalCharInName = Validator.createRegexValidator(
                "Der Nutzername enthält nicht zulässige Zeichen!",
                "^[a-zA-z0-9\\söäüß]*$",
                Severity.ERROR);

        usernameValidation.registerValidator(usernameField, Validator.combine(illegalCharInName, emptyName));

        Validator<String> emptyPassword = Validator.createEmptyValidator("Es muss ein Passwort angegeben werden!");
        passwordValidation.registerValidator(passwordField, true, emptyPassword);

        //passwordValidation.setErrorDecorationEnabled(false);

//        Validator<String> uniqueName = (c, val) -> {
//            return new ValidationResult().addErrorIf(c, "The name must not contain xyzyy", val.contains("xyz"));
//        };
    }



    private boolean isValidInput() {

        LoginManager.styleOnValid(usernameValidation, usernameField, "bad-input");
        LoginManager.styleOnValid(passwordValidation, passwordField, "bad-input");

        return !usernameValidation.isInvalid() && !passwordValidation.isInvalid();
    }

}
