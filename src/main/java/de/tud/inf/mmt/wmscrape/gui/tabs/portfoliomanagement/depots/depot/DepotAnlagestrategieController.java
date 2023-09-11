package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.depot;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class DepotAnlagestrategieController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public DepotAnlagestrategieController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }
}
