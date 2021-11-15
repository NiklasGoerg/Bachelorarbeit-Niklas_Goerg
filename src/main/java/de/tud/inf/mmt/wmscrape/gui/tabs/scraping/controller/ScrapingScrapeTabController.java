package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class ScrapingScrapeTabController {

    //@FXML private TreeView
    @FXML private TextArea logArea;

    @Autowired
    private WebsiteRepository websiteRepository;

    private static SimpleStringProperty logText;

    @FXML
    private void initialize() {
        logText = new SimpleStringProperty("");
        logArea.clear();
        logArea.textProperty().bind(logText);
    }

    @FXML
    @Transactional
    public void handleStartButton() {
        Website website = fresh();
        WebsiteElement element = website.getWebsiteElements().get(0);

        WebsiteScraper scraper = new WebsiteScraper(website, logText, false);
        scraper.processElement(element);
    }

    private Website fresh() {
        return websiteRepository.findByDescription("localhost").get();
    }

    @FXML
    private void handleNextButton() {

    }
}
