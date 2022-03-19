package de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class HistoricTabController {
    @FXML private TabPane historicSubTabPane;
    @Autowired private HistoricWebsiteTabController historicWebsiteTabController;
    @Autowired private HistoricWebsiteElementTabController historicWebsiteElementTabController;
    @Autowired private HistoricScrapeTabController historicScrapeTabController;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() throws IOException {
        Parent parent = PrimaryTabManager.loadTabFxml("gui/tabs/historic/controller/historicScrapeTab.fxml", historicScrapeTabController);
        Tab tab = new Tab("Scrapen" , parent);
        tab.selectedProperty().addListener((o,ov,nv) -> historicScrapeTabController.refresh());
        historicSubTabPane.getTabs().add(tab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/historic/controller/historicWebsitesTab.fxml", historicWebsiteTabController);
        tab = new Tab("Webseiten" , parent);
        historicSubTabPane.getTabs().add(tab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/historic/controller/historicElementsTab.fxml", historicWebsiteElementTabController);
        tab = new Tab("Elemente" , parent);
        tab.selectedProperty().addListener((o,ov,nv) -> historicWebsiteElementTabController.refresh());
        historicSubTabPane.getTabs().add(tab);
    }
}
