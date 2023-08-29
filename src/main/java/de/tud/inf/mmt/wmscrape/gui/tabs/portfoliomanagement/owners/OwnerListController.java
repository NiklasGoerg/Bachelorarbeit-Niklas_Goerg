package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners;

import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.PortfolioManagementTabManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class OwnerListController {

    @FXML
    PortfolioManagementTabManager portfolioManagementTabManager;

    @FXML
    Button switchSceneButton;

    @Autowired
    public OwnerListController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    @FXML
    private void initialize() {
        switchSceneButton.setOnAction(actionEvent -> {
            portfolioManagementTabManager.showInhaberTabs();
        });
    }

}
