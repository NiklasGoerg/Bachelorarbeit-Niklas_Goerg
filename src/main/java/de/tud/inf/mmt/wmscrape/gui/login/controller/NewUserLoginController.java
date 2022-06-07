package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NewUserLoginController {

    @FXML private TextField rootUsernameField;
    @FXML private PasswordField rootPasswordField;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;

    @FXML private ProgressIndicator progress;
    @FXML private Button createButton;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() {
        rootUsernameField.textProperty().addListener(x -> validRootUsernameField());
        rootPasswordField.textProperty().addListener(x -> validRootPasswordField());
        newUsernameField.textProperty().addListener(x -> validNewUsernameField());
        newPasswordField.textProperty().addListener(x -> validNewPasswordField());

        showLoginProgress(false);
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
                            newUsernameField.getText().trim().replace(" ", "_").toLowerCase()
                            + "_wms_db wurde angelegt.\nOk drücken zum Einloggen.";
            }
            case -1 -> {
                alertHeaderText = "Administrator-Daten fehlerhaft";
                alertText = "Überprüfen Sie die Anmeldedaten des Administrators und den Pfad!";
            }
            case -2 -> {
                alertHeaderText = "Nicht als Administrator verbunden!";
                alertText = "Die Rechte des \"Administrators\" sind nicht ausreichend.";
            }
            case -3 -> {
                alertHeaderText = "Wählen Sie einen anderen Nutzernahmen.";
                alertText = "Der Nutzer mit dem Namen " + newUsernameField.getText().trim() +
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
        var window = rootUsernameField.getScene().getWindow();
        alert.setX(window.getX()+(window.getWidth()/2)-200);
        alert.setY(window.getY()+(window.getHeight()/2)-200);
        alert.showAndWait();

        if(returnInformation > 0) {
            try {
                LoginManager.loginAsUser(newUsernameField.getText(), newPasswordField.getText(), progress, createButton);
                showLoginProgress(true);
            } catch (Exception e) {
                LoginManager.programErrorAlert(e, newUsernameField);
            }
        }
    }

    @FXML
    private void handleBackButton() {
        PrimaryTabManager.loadFxml("gui/login/controller/existingUserLogin.fxml", "Login",
                rootPasswordField, false, null, false);
    }

    @FXML
    private void handleChangeDbPathButton() {
        PrimaryTabManager.loadFxml("gui/login/controller/changeDbPathPopup.fxml",
                "Datenbankpfad ändern", rootPasswordField, true, null, false);
    }

    private boolean isValidInput() {
        // evaluate all to highlight all
        boolean valid = validRootUsernameField();
        valid &= validRootPasswordField();
        valid &= validNewUsernameField();
        valid &= validNewPasswordField();
        return valid;
    }

    private boolean validRootUsernameField() {
        // no username validation as the user will be created otherwise and could have other symbols
        // doesnt matter in this case
        return emptyValidator(rootUsernameField); //&& usernameValidator(rootUsernameField);
    }

    private boolean validRootPasswordField() {
        return emptyValidator(rootPasswordField);
    }

    private boolean validNewUsernameField() {
        return emptyValidator(newUsernameField) && usernameValidator(newUsernameField);
    }

    private boolean validNewPasswordField() {
        return emptyValidator(newPasswordField);
    }

    private boolean emptyValidator(TextInputControl input) {
        boolean isValid = input.getText() != null && !input.getText().isBlank();
        PrimaryTabManager.decorateField(input, "Dieses Feld darf nicht leer sein!", isValid, true);
        return isValid;
    }

    private boolean usernameValidator(TextInputControl input) {
        String value = input.getText();
        if(value==null) return true;

        boolean isValid = value.matches("^[a-zA-Z0-9\\söäüß]*$");
        PrimaryTabManager.decorateField(input, "Nicht zulässige Zeichen! Nur a-z,0-9,ä,ö,ü,ß sowie Leerzeichen sind erlaubt.",
                                        isValid, true);
        return isValid;
    }

    /**
     * hides the create user button and shows the progress while waiting for spring to complete initialization
     * @param show if true progress is displayed
     */
    private void showLoginProgress(boolean show) {
        createButton.setVisible(!show);
        createButton.setManaged(!show);
        progress.setVisible(show);
        progress.setManaged(show);
        progress.setProgress(-1);
    }


}
