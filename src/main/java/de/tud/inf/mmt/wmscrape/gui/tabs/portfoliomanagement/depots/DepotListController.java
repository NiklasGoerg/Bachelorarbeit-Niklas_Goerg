package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots;

import de.tud.inf.mmt.wmscrape.WMScrape;
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
    private Button switchSceneButton;
    @FXML
    private PortfolioManagementTabManager portfolioManagementTabManager;

    @Autowired
    public DepotListController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        switchSceneButton.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showDepotTabs();
        });
    }

}
