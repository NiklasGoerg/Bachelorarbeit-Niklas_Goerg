package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.controller;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElementRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingTabManager {

    @Autowired
    private WebsiteRepository websiteRepository;
    @Autowired
    protected WebsiteElementRepository websiteElementRepository;

    public Website createNewWebsite(String description) {
        Website website = new Website(description);
        websiteRepository.save(website);
        return website;
    }

    public WebsiteElement createNewElement(String description, ContentType contentType, MultiplicityType multiplicityType) {
        WebsiteElement element = new WebsiteElement(description, contentType, multiplicityType);
        websiteElementRepository.save(element);
        return element;
    }

    public void deleteSpecificWebsite(Website website) {
        // fix for not working orphan removal
        website.setWebsiteElements(new ArrayList<>());
        websiteRepository.delete(website);
    }

    public void deleteSpecificElement(WebsiteElement element) {
        element.setElementSelections(new ArrayList<>());
        element.setElementCorrelations(new ArrayList<>());
        websiteElementRepository.delete(element);
    }

    public ObservableList<Website> initWebsiteList(ListView<Website> websiteListView) {
        ObservableList<Website> websiteObservableList = FXCollections.observableList(websiteRepository.findAll());
        websiteListView.setItems(websiteObservableList);
        return websiteObservableList;
    }

    public ObservableList<WebsiteElement> initWebsiteElementList(ListView<WebsiteElement> elementListView) {
        ObservableList<WebsiteElement> elementObservableList = FXCollections.observableList(websiteElementRepository.findAll());
        elementListView.setItems(elementObservableList);
        return elementObservableList;
    }

    public List<Website> getWebsites() {
        return websiteRepository.findAll();
    }

    public List<WebsiteElement> getElements() {
        return websiteElementRepository.findAll();
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

    public static void loadSubMenu(Object controllerClass, String resource, BorderPane control) {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(resource));
        fxmlLoader.setControllerFactory(param -> controllerClass);
        Parent scene;

        try {
            scene = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        control.centerProperty().setValue(scene);
    }

    @Transactional
    public void resetElement(TextField urlField, ChoiceBox<Website> choiceBox, WebsiteElement oldElement) {
        var newElement = getFreshWebsiteElement(oldElement);
        oldElement.setTableIdent(newElement.getTableIdent());
        oldElement.setTableIdenType(newElement.getTableIdenType());
        urlField.setText(newElement.getInformationUrl());
        choiceBox.setValue(newElement.getWebsite());
    }

    protected WebsiteElement getFreshWebsiteElement(WebsiteElement staleElement) {
        return websiteElementRepository.getById(staleElement.getId());
    }

}
