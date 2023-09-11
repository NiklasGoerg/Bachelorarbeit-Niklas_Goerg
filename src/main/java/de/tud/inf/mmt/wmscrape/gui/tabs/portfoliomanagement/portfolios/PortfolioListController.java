package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class PortfolioListController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @FXML
    Button switchToPortfolio1Button;
    @FXML
    Button switchToPortfolio2Button;
    @Autowired
    public PortfolioListController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        switchToPortfolio1Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showPortfolioTabs();
            portfolioManagementTabManager.setTypeOfCurrentlyDisplayedElement("portfolio");
            portfolioManagementTabManager.setCurrentlyDisplayedElement("Portfolio 1");
        });
        switchToPortfolio2Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showPortfolioTabs();
            portfolioManagementTabManager.setTypeOfCurrentlyDisplayedElement("portfolio");
            portfolioManagementTabManager.setCurrentlyDisplayedElement("Portfolio 2");
        });
    }

}
