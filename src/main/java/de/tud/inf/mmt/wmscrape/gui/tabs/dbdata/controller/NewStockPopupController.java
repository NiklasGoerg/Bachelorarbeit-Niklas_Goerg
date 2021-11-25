package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.StockAndCourseTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class NewStockPopupController {
    @FXML private TextField isinField;
    @FXML private TextField wknField;
    @FXML private TextField nameField;
    @FXML private TextField typeField;

    @Autowired
    private StockAndCourseTabManager manager;
    @Autowired
    private StockTabController tabController;
    @Autowired
    private ScrapingElementsTabController elementsTabController;
    
    @FXML
    private void initialize() {
        isinField.textProperty().addListener((o,ov,nv) -> { if (nv != null) isValidIsin();});
    }

    @FXML
    private void handleCancelButton() {
        isinField.getScene().getWindow().hide();
    }

    @FXML
    private void handleConfirmButton() {
        if(!isValidIsin()) {
            return;
        }

        manager.createStock(isinField.getText(),wknField.getText(),nameField.getText(), typeField.getText());
        tabController.handleResetButton();
        elementsTabController.refresh();

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Ein neues Wertpapeir wurde angelegt.", ButtonType.OK);
        alert.setHeaderText("Wertpapier angelegt!");
        alert.setY(isinField.getScene().getWindow().getY() + (isinField.getScene().getWindow().getHeight() / 2) - 200);
        alert.setX(isinField.getScene().getWindow().getX() + (isinField.getScene().getWindow().getWidth() / 2) - 200);
        alert.showAndWait();

        isinField.getScene().getWindow().hide();
    }

    private boolean isValidIsin() {
        isinField.getStyleClass().remove("bad-input");
        isinField.setTooltip(null);

        if(isinField.getText() == null || isinField.getText().isBlank()) {
            isinField.setTooltip(manager.createTooltip("Dieses Feld darf nicht leer sein!"));
            isinField.getStyleClass().add("bad-input");
            return false;
        }
        return true;
    }
}
