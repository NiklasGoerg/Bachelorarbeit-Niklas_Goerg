package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.management.ImportTabManagement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class NewExcelPopupController {
    @FXML
    private TextField descriptionField;

    @Autowired
    private ImportTabManagement importTabManagement;
    @Autowired
    private ImportTabController importTabController;

    ValidationSupport dbPathValidation = new ValidationSupport();

    @FXML
    private void initialize() {
        addValidation();
    }

    @FXML
    private void handleCancelButton() {
        descriptionField.getScene().getWindow().hide();
    }

    @FXML
    private void handleConfirmButton() {
        if(isValidInput()) {
            importTabManagement.createNewExcel(descriptionField.getText());
        }

        importTabController.reloadExcelList();
        importTabController.selectLastExcel();

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Eine leere Exceltabellenbeschreibung wurde angelegt", ButtonType.OK);
        alert.setHeaderText("Excel angelegt!");
        alert.showAndWait();

        descriptionField.getScene().getWindow().hide();
    }

    private void addValidation() {
        Validator<String> emptyPath = Validator.createEmptyValidator("Es muss eine Beschreibung angegeben werden!");
        dbPathValidation.registerValidator(descriptionField, emptyPath);
    }

    private boolean isValidInput() {
        LoginManager.styleOnValid(dbPathValidation, descriptionField, "bad-input");
        return !dbPathValidation.isInvalid();
    }
}
