package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class PortfolioKontosController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public PortfolioKontosController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }
}
