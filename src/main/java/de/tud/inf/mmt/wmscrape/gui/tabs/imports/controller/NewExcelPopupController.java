package de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.management.ImportTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class NewExcelPopupController {
    @FXML
    private TextField descriptionField;

    @Autowired
    private ImportTabManager importTabManager;
    @Autowired
    private ImportTabController importTabController;

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

        importTabManager.createNewExcel(descriptionField.getText());
        importTabController.reloadExcelList();
        importTabController.selectLastExcel();

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Eine leere Exceltabellenbeschreibung wurde angelegt", ButtonType.OK);
        alert.setHeaderText("Excel angelegt!");
        var window = descriptionField.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
        alert.showAndWait();

        window.hide();
    }

    private boolean isValidDescription() {
        descriptionField.getStyleClass().remove("bad-input");
        descriptionField.setTooltip(null);

        if(descriptionField.getText() == null || descriptionField.getText().isBlank()) {
            descriptionField.setTooltip(PrimaryTabManager.createTooltip("Dieses Feld darf nicht leer sein!"));
            descriptionField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }
}
