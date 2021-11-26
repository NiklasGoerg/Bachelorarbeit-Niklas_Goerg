package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.StockDataManager;
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
    private StockDataManager manager;
    @Autowired
    private DataTabController tabController;
    @Autowired
    private ScrapingElementsTabController elementsTabController;
    @Autowired
    private DataTabController dataTabController;
    
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
        var window = isinField.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
        alert.showAndWait();

        window.hide();
    }

    private boolean isValidIsin() {
        isinField.getStyleClass().remove("bad-input");
        isinField.setTooltip(null);

        String text = isinField.getText();

        if(text == null || text.isBlank()) {
            isinField.setTooltip(PrimaryTabManagement.createTooltip("Dieses Feld darf nicht leer sein!"));
            isinField.getStyleClass().add("bad-input");
            return false;
        } else if (text.length()>=50) {
            isinField.setTooltip(PrimaryTabManagement.createTooltip("Die maximale Länge der ISIN beträgt 50 Zeichen."));
            isinField.getStyleClass().add("bad-input");
        }

        return true;
    }
}
