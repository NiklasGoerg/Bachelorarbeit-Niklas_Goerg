package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.depot.planung;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class DepotPlanungOrderController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public DepotPlanungOrderController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }
}
