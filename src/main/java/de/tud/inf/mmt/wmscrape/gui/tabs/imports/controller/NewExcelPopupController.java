package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.management.ImportTabManagement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.controlsfx.validation.ValidationSupport;
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
        descriptionField.textProperty().addListener((o,ov,nv) -> { if (nv != null) isValidDescription();});
    }

    @FXML
    private void handleCancelButton() {
        descriptionField.getScene().getWindow().hide();
    }

    @FXML
    private void handleConfirmButton() {
        if(!isValidDescription()) {
            return;
        }

        importTabManagement.createNewExcel(descriptionField.getText());
        importTabController.reloadExcelList();
        importTabController.selectLastExcel();

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Eine leere Exceltabellenbeschreibung wurde angelegt", ButtonType.OK);
        alert.setHeaderText("Excel angelegt!");
        alert.showAndWait();

        descriptionField.getScene().getWindow().hide();
    }

    private boolean isValidDescription() {
        descriptionField.getStyleClass().remove("bad-input");
        descriptionField.setTooltip(null);

        if(descriptionField.getText() == null || descriptionField.getText().isBlank()) {
            descriptionField.setTooltip(importTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            descriptionField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }
}
