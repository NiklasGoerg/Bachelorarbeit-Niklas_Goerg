package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.ScrapingElementsTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.website.ScrapingWebsiteTabController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class ScrapingTabController {
    @FXML private TabPane scrapingSubTabPane;
    @Autowired private ScrapingWebsiteTabController scrapingWebsiteTabController;
    @Autowired private ScrapingElementsTabController scrapingElementsTabController;
    @Autowired private ScrapingScrapeTabController scrapingScrapeTabController;
    @Autowired private PrimaryTabManagement primaryTabManagement;

    @FXML
    private void initialize() throws IOException {
        Parent parent = primaryTabManagement.loadTabFxml("gui/tabs/scraping/controller/scrapingScrapeTab.fxml", scrapingScrapeTabController);
        Tab tab = new Tab("Scrapen" , parent);
        scrapingSubTabPane.getTabs().add(tab);

        parent = primaryTabManagement.loadTabFxml("gui/tabs/scraping/controller/website/scrapingWebsitesTab.fxml", scrapingWebsiteTabController);
        tab = new Tab("Webseiten" , parent);
        scrapingSubTabPane.getTabs().add(tab);

        parent = primaryTabManagement.loadTabFxml("gui/tabs/scraping/controller/element/scrapingElementsTab.fxml", scrapingElementsTabController);
        tab = new Tab("Elemente" , parent);
        scrapingSubTabPane.getTabs().add(tab);
    }
}