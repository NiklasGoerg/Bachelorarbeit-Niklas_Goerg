package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebsiteManager {

    @Autowired
    private WebsiteRepository websiteRepository;

    public Website createNewWebsite(String description) {
        Website website = new Website(description);
        websiteRepository.save(website);
        return website;
    }

    public void deleteSpecificWebsite(Website website) {
        websiteRepository.delete(website);
    }

    public List<Website> getWebsites(boolean historic) {
        return historic ?
                websiteRepository.findAllByIsHistoricTrue() :
                websiteRepository.findAllByIsHistoricFalse();
    }

    public void saveWebsite(Website website) {
        websiteRepository.save(website);
    }

    public ObservableList<Website> initWebsiteList(ListView<Website> websiteListView, boolean historic) {
        ObservableList<Website> websiteObservableList = FXCollections.observableList(getWebsites(historic));
        websiteListView.setItems(websiteObservableList);
        return websiteObservableList;
    }
}
