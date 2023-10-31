package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.BreadcrumbElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class PortfolioKontosController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;
    @FXML
    VBox kontoListContainer;
    @Autowired
    public PortfolioKontosController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        //init depots
        for(int i = 0; i < portfolioManagementTabManager.kontosOfPortfolio1List.length; i++) {
            String konto = portfolioManagementTabManager.kontosOfPortfolio1List[i];
            Button button = new Button(konto);
            button.setOnAction(actionEvent -> {
                portfolioManagementTabManager.showKontoTabs();
                portfolioManagementTabManager.addCurrentlyDisplayedElement(new BreadcrumbElement(konto, "konto"));
            });

            kontoListContainer.getChildren().add(button);
        }
    }
}
