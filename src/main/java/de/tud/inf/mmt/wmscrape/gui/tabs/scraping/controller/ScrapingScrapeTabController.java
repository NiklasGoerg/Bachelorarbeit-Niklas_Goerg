package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

@Controller
public class ScrapingScrapeTabController {

    //@FXML private TreeView
    @FXML private TextArea logArea;

    //TODO remove
    @Autowired
    DataSource dataSource;

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
        Optional<Website> website = fresh();

        if(website.isEmpty()) return;

        try {
            // TODO close datasource
            WebsiteScraper scraper = new WebsiteScraper(website.get(), logText, false, dataSource.getConnection());
            scraper.processWebsite();
            scraper.quit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Optional<Website> fresh() {
        return websiteRepository.findByDescription("localhost");
    }

    @FXML
    private void handleNextButton() {

    }
}
