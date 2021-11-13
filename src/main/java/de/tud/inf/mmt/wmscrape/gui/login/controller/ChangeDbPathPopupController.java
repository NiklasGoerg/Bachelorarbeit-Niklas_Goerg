package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.*;
import java.util.Properties;

public class ChangeDbPathPopupController {

    @FXML private TextField dbPathField;

    private Properties properties;
    ValidationSupport dbPathValidation = new ValidationSupport();

    @FXML
    private void initialize() throws IOException {
        addValidation();

        properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/user.properties"));
        String lastDbPath = properties.getProperty("last.dbPath", "mysql://localhost/");
        dbPathField.setText(lastDbPath);
    }

    @FXML
    private void handleConfirmButton() throws IOException {
        if(!isValidInput()) {
            return;
        }

        String newPath = dbPathField.getText();
        properties.setProperty("last.dbPath", newPath);
        properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        SpringIndependentData.setPropertyConnectionPath(newPath);
        closeWindow();
    }

    @FXML
    private void closeWindow() {
        LoginManager.closeWindow(dbPathField);
    }

    private void addValidation() {
        Validator<String> emptyPath = Validator.createEmptyValidator("Es muss ein Pfad angegeben werden!");
        dbPathValidation.registerValidator(dbPathField, emptyPath);
    }

    private boolean isValidInput() {
        LoginManager.styleOnValid(dbPathValidation, dbPathField, "bad-input");

        return !dbPathValidation.isInvalid();
    }
}
