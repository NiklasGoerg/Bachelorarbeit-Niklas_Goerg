package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.kontos.konto;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class KontoOverviewController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public KontoOverviewController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }
}
