package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.BreadcrumbElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;


@Controller
public class DepotListController {
    @FXML
    private Button switchToDepot1Button;
    @FXML
    private Button switchToDepot2Button;
    @FXML
    private Button switchToDepot3Button;
    @FXML
    private PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public DepotListController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        switchToDepot1Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showDepotTabs();
            portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement("Depot 1", "depot"));
        });
        switchToDepot2Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showDepotTabs();
            portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement("Depot 2", "depot"));
        });
        switchToDepot3Button.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showDepotTabs();
            portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement("Depot 3", "depot"));
        });
    }

}
