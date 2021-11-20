package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class NewUserLoginController {

    @FXML private TextField rootUsernameField;
    @FXML private PasswordField rootPasswordField;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;

    ValidationSupport rootUsernameValidation = new ValidationSupport();
    ValidationSupport rootPasswordValidation = new ValidationSupport();
    ValidationSupport newUsernameValidation = new ValidationSupport();
    ValidationSupport newPasswordValidation = new ValidationSupport();

    @FXML
    private void initialize() {
        addValidation();
    }

    @FXML
    private void handleCreateUserButton() {
        if(!isValidInput()) {
            return;
        }
        int returnInformation = LoginManager.createUser(
                rootUsernameField.getText(),
                rootPasswordField.getText(),
                newUsernameField.getText(),
                newPasswordField.getText());

        String alertText;
        String alertHeaderText;
        Alert.AlertType alertType = Alert.AlertType.ERROR;

        switch (returnInformation) {
            case 1 -> {
                alertType = Alert.AlertType.INFORMATION;
                alertHeaderText = "Nutzer angelegt!";
                alertText = "Ein neuer Nutzer mit der Datenbank " +
                            newUsernameField.getText().trim().replace(" ", "_")
                            + "_USER_DB wurde angelegt.\nOk drücken zum Einloggen.";
            }
            case -1 -> {
                alertHeaderText = "Administratordaten fehlerhaft";
                alertText = "Überprüfen Sie die Anmeldedaten des Administrators und den Pfad!";
            }
            case -2 -> {
                alertHeaderText = "Nicht als Administrator verbunden!";
                alertText = "Die Rechte des Administrators sind nicht ausreichend. Kontaktieren Sie" +
                        " den Datenbankbetreiber.";
            }
            case -3 -> {
                alertText = "Wählen Sie einen anderen Nutzernahmen.";
                alertHeaderText = "Der Nutzer mit dem Namen " + newUsernameField.getText().trim() +
                        " existiert bereits!";
            }
            case -4 -> {
                alertHeaderText = "Nutzerdatenbank existiert bereits!";
                alertText = "Eine Datenbank für den angegeben Nutzer existiert bereits. Löschen Sie die Datenbank " +
                        "und probieren Sie es erneut.";
            }
            case -5 -> {
                alertHeaderText = "Unbekannter Fehler!";
                alertText = "Bei der Erzeugung des Nutzers und dessen Datenbank kam es zu einem unbekannten Fehler.";
            }
            default -> {
                alertHeaderText = "Unbekannter Fehler!";
                alertText = "Keine Beschreibung verfügbar.";
            }
        }

        Alert alert = new Alert(alertType, alertText, ButtonType.OK);
        alert.setHeaderText(alertHeaderText);
        alert.setX(rootUsernameField.getScene().getWindow().getX()+(rootUsernameField.getScene().getWindow().getWidth()/2)-200);
        alert.setY(rootUsernameField.getScene().getWindow().getY()+(rootUsernameField.getScene().getWindow().getHeight()/2)-200);
        alert.showAndWait();

        if(returnInformation > 0) {
            LoginManager.loginExistingUser(newUsernameField.getText(), newPasswordField.getText(), rootUsernameField);
        }
    }

    @FXML
    private void handleBackButton() {
        LoginManager.loadFxml("gui/login/controller/existingUserLogin.fxml", "Login", rootPasswordField, false);
    }

    @FXML
    private void handleChangeDbPathButton() {
        LoginManager.loadFxml("gui/login/controller/changeDbPathPopup.fxml", "Datenbankpfad ändern", rootPasswordField, true);
    }

    private void addValidation() {

        Validator<String> emptyName = Validator.createEmptyValidator("Es muss ein Nutzername angegeben werden!");
        Validator<String> illegalCharInName = Validator.createRegexValidator(
                "Der Nutzername enthält nicht zulässige Zeichen!",
                "^[a-zA-z0-9\\söäüß]*$",
                Severity.ERROR);

        rootUsernameValidation.registerValidator(rootUsernameField, Validator.combine(illegalCharInName, emptyName));
        newUsernameValidation.registerValidator(newUsernameField, Validator.combine(illegalCharInName, emptyName));


        Validator<String> emptyPassword = Validator.createEmptyValidator("Es muss ein Passwort angegeben werden!");
        rootPasswordValidation.registerValidator(rootPasswordField, emptyPassword);
        newPasswordValidation.registerValidator(newPasswordField, emptyPassword);

    }

    private boolean isValidInput() {

        LoginManager.styleOnValid(rootUsernameValidation, rootUsernameField, "bad-input");
        LoginManager.styleOnValid(rootPasswordValidation, rootPasswordField, "bad-input");
        LoginManager.styleOnValid(newUsernameValidation, newUsernameField, "bad-input");
        LoginManager.styleOnValid(newPasswordValidation, newPasswordField, "bad-input");

        return !rootUsernameValidation.isInvalid() &&
                !rootPasswordValidation.isInvalid() &&
                !newUsernameValidation.isInvalid() &&
                !newPasswordValidation.isInvalid();
    }
}
