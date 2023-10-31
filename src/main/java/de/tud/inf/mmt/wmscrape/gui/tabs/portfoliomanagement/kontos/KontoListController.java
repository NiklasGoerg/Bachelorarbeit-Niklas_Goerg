package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.kontos;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.BreadcrumbElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class KontoListController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @FXML
    Button switchToKonto1Button;
    @FXML
    Button switchToKonto2Button;

    @Autowired
    public KontoListController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        switchToKonto1Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showKontoTabs();
            portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement("Konto 1", "konto"));
        });
        switchToKonto2Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showKontoTabs();
            portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement("Konto 2", "konto"));
        });
    }

}
