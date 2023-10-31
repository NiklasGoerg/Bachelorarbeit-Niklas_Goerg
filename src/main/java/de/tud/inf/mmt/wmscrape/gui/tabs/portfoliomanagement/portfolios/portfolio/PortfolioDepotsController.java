package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.BreadcrumbElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.PortfolioListController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.awt.*;

@Controller
public class PortfolioDepotsController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;
    @FXML
    VBox depotListContainer;

    @Autowired
    public PortfolioDepotsController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        //init depots
        for(int i = 0; i < portfolioManagementTabManager.depotsOfPortfolio1List.length; i++) {
            String depot = portfolioManagementTabManager.depotsOfPortfolio1List[i];
            Button button = new Button(depot);
            button.setOnAction(actionEvent -> {
                portfolioManagementTabManager.showDepotTabs();
                portfolioManagementTabManager.addCurrentlyDisplayedElement(new BreadcrumbElement(depot, "depot"));
            });

            depotListContainer.getChildren().add(button);
        }
    }
}
