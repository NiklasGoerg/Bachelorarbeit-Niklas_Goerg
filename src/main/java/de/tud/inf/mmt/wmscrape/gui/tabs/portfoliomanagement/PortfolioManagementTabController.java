package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller.DataTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.HistoricTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.DepotListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.kontos.KontoListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners.OwnerListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.PortfolioListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller.VisualizationTabController;
import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;

@Controller
public class PortfolioManagementTabController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button switchSceneButton;
    @FXML
    private TabPane portfolioManagementTabPane;
    @FXML
    private Label currentUserLabel;

    @Autowired
    private PortfolioListController portfolioListController;
    @Autowired
    private DepotListController depotListController;
    @Autowired
    private KontoListController kontoListController;
    @Autowired
    private OwnerListController ownerListController;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() throws IOException {

        Parent parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolios.fxml", portfolioListController);
        Tab dataTab = createStyledTab("Portfolios", parent);
        portfolioManagementTabPane.getTabs().add(dataTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depots.fxml", depotListController);
        Tab importTab = createStyledTab("Depots", parent);
        portfolioManagementTabPane.getTabs().add(importTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/kontos.fxml", kontoListController);
        Tab tab = createStyledTab("Konten", parent);
        portfolioManagementTabPane.getTabs().add(tab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owners.fxml", ownerListController);
        Tab historicTab = createStyledTab("Inhaber", parent);
        portfolioManagementTabPane.getTabs().add(historicTab);


        portfolioManagementTabPane.setStyle("-fx-tab-min-height: 30px;" + "-fx-tab-max-height: 30px;" + "-fx-tab-min-width: 150px;" + "-fx-tab-max-width: 150px;" + "-fx-alignment: CENTER;");
        // Anpassen des Schriftstils der Tabs
        portfolioManagementTabPane.getTabs().forEach(pmTab -> {
            pmTab.setStyle("-fx-text-fill: white;");
        });
    }

    // Hilfsmethode zur Erstellung von Tabs mit angepasstem Stil
    private Tab createStyledTab(String title, Parent parent) {
        Tab tab = new Tab(title, parent);
        tab.setStyle("-fx-background-color: #FFF;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                tab.setStyle("-fx-background-color: #DCDCDC;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
            } else {
                tab.setStyle("-fx-background-color: #FFF;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
            }
        });


        return tab;
    }
}
