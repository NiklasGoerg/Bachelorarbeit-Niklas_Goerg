package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners.owner;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class OwnerKontosController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public OwnerKontosController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }
}
