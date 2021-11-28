package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ChangeDbPathPopupController {

    @FXML private TextField dbPathField;

    private Properties properties;

    @FXML
    private void initialize() throws IOException {
        dbPathField.textProperty().addListener(x -> dbPathValidation());
        properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/user.properties"));
        String lastDbPath = properties.getProperty("last.dbPath", "mysql://localhost/");
        dbPathField.setText(lastDbPath);
    }

    @FXML
    private void handleConfirmButton() throws IOException {
        if(!dbPathValidation()) {
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

    private boolean dbPathValidation() {
        boolean isValid = dbPathField.getText() != null && !dbPathField.getText().isBlank();
        decorateField(dbPathField, isValid);
        return isValid;
    }

    private void decorateField(TextInputControl input, boolean isValid) {
        input.getStyleClass().remove("bad-input");
        input.setTooltip(null);

        if(!isValid) {
            input.setTooltip(PrimaryTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            input.getStyleClass().add("bad-input");
        }
    }
}
