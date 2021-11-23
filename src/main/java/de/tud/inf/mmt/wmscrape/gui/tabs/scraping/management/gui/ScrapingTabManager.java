package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebRepresentation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteTree;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElementRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ScrapingTabManager {

    @Autowired
    private WebsiteRepository websiteRepository;
    @Autowired
    private WebsiteElementRepository websiteElementRepository;
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @Autowired
    private DataSource dataSource;
    private WebsiteScraper scrapingService;
    private Connection dbConnection;


    private static final List<Worker.State> RUNNING_STATES = new ArrayList<>(Arrays.asList(Worker.State.RUNNING, Worker.State.SCHEDULED));

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

    private WebsiteElement getFreshWebsiteElement(WebsiteElement staleElement) {
        return websiteElementRepository.getById(staleElement.getId());
    }

    @Transactional
    public TreeView<WebRepresentation<?>> initSelectionTree(
            ObservableMap<Website, ObservableList<WebsiteElement>> checkedItems) {
        var websites = getWebsites();
        return (new WebsiteTree(websites, checkedItems)).getTreeView();
    }


    /*

    This is where the transition from the fui to the scraper happens

     */
    public void startScrape(double minIntra, double maxIntra, double waitElement, boolean pauseAfterElement,
                            SimpleStringProperty logText, Boolean headless,
                            ObservableMap<Website, ObservableList<WebsiteElement>> checkedItems) {

        if(scrapingService != null) {
            //if (RUNNING_STATES.contains(scrapingService.stateProperty().get())) return;
            cancelScrape();
        }

        if(!updateDbConnection()) return;


        scrapingService = new WebsiteScraper(logText, headless, dbConnection, pauseAfterElement);
        beanFactory.autowireBean(scrapingService);


        scrapingService.setMinIntraSiteDelay(minIntra);
        scrapingService.setMaxIntraSiteDelay(maxIntra);
        scrapingService.setWaitForWsElementSec(waitElement);
        scrapingService.resetData(checkedItems);

        logText.set("");
        scrapingService.start();
    }

    public void cancelScrape() {
        if(scrapingService != null) {
            scrapingService.cancel();
            scrapingService.quit();
            scrapingService = null;
        }
    }

    public void continueScrape(double minIntra, double maxIntra, double waitElement, boolean pauseElement) {

        if(scrapingService != null) {
            if (scrapingService.stateProperty().get() == Worker.State.SUCCEEDED) {
                scrapingService.setMinIntraSiteDelay(minIntra);
                scrapingService.setMaxIntraSiteDelay(maxIntra);
                scrapingService.setWaitForWsElementSec(waitElement);
                scrapingService.setPauseAfterElement(pauseElement);
                // continues where it stopped
                scrapingService.restart();
            } else if(scrapingService.stateProperty().get() != Worker.State.RUNNING) {
                cancelScrape();
            }
        }
    }

    private boolean updateDbConnection(){
        try {
            if(dbConnection == null || dbConnection.isClosed()) {
                dbConnection = dataSource.getConnection();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
