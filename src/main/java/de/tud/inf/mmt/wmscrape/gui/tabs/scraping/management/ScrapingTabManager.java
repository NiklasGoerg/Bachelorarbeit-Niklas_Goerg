package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingTabManager {

    @Autowired
    private WebsiteRepository websiteRepository;

    public Website createNewWebsite(String description) {
        Website website = new Website(description);
        websiteRepository.save(website);
        return website;
    }

    public void deleteSpecificWebsite(Website website) {
        // fix for not working orphan removal
        website.setWebsiteElements(new ArrayList<>());
        websiteRepository.delete(website);
    }

    public ObservableList<Website> initWebsiteList(ListView<Website> websiteListView) {
        ObservableList<Website> websiteObservableList = FXCollections.observableList(websiteRepository.findAll());
        websiteListView.setItems(websiteObservableList);
        return websiteObservableList;
    }

    public List<Website> getWebsites() {
        return websiteRepository.findAll();
    }

    public Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle(".bad-input");
        return tooltip;
    }

    public void saveWebsite(Website website) {
        websiteRepository.save(website);
    }
}
