package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.helper.PropertiesHelper;
import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ChangeDbPathPopupController {

    @FXML private TextField dbPathField;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() {
        dbPathField.textProperty().addListener(x -> dbPathValidation());
        String lastDbPath = PropertiesHelper.getProperty("last.dbPath", "mysql://localhost/");
        dbPathField.setText(lastDbPath);
    }

    /**
     * stores the db connection path
     */
    @FXML
    private void handleConfirmButton() {
        if(!dbPathValidation()) return;

        String newPath = dbPathField.getText();
        PropertiesHelper.setProperty("last.dbPath", newPath);

        SpringIndependentData.setPropertyConnectionPath(newPath);
        closeWindow();
    }

    /**
     * closes the popup window
     */
    @FXML
    private void closeWindow() {
        dbPathField.getScene().getWindow().hide();
    }

    /**
     * @return true if the path field contains a valid path
     */
    private boolean dbPathValidation() {
        boolean isValid = dbPathField.getText() != null && !dbPathField.getText().isBlank();
        PrimaryTabManager.decorateField(dbPathField, "Es muss ein Pfad angeben werden.", isValid, true);
        return isValid;
    }
}
