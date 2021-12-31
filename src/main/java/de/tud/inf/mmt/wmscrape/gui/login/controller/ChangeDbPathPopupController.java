package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
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

    /**
     * called when loading the fxml file
     * @throws IOException if the properties can't be read
     */
    @FXML
    private void initialize() throws IOException {
        dbPathField.textProperty().addListener(x -> dbPathValidation());
        properties = new Properties();
        properties.load(new FileInputStream("src/main/resources/user.properties"));
        String lastDbPath = properties.getProperty("last.dbPath", "mysql://localhost/");
        dbPathField.setText(lastDbPath);
    }

    /**
     * stores the db connection path
     * @throws IOException if the properties can't be written
     */
    @FXML
    private void handleConfirmButton() throws IOException {
        if(!dbPathValidation()) return;

        String newPath = dbPathField.getText();
        properties.setProperty("last.dbPath", newPath);
        properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
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
        decorateField(dbPathField, isValid);
        return isValid;
    }

    /**
     * highlights an invalid filed
     * @param input the checked field
     * @param isValid true if the field is valid
     */
    private void decorateField(TextInputControl input, boolean isValid) {
        input.getStyleClass().remove("bad-input");
        input.setTooltip(null);

        if(!isValid) {
            input.setTooltip(PrimaryTabManager.createTooltip("Dieses Feld darf nicht leer sein!"));
            input.getStyleClass().add("bad-input");
        }
    }
}
