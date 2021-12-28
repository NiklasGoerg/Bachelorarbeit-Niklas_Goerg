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
import javafx.collections.ObservableSet;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
    private Properties properties;

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
        // fix for not working orphan removal. PS: could be fixed with cascade typ. idk
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
    public void resetElementRepresentation(TextField urlField, ChoiceBox<Website> choiceBox, WebsiteElement oldElement) {
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
    public TreeView<WebRepresentation<?>> createSelectionTree(
            ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems, Set<Integer> restored) {
        var websites = getWebsites();
        return (new WebsiteTree(websites, checkedItems, restored)).getTreeView();
    }


    public Properties getProperties() {
        if(properties == null) {
            try(FileInputStream f = new FileInputStream("src/main/resources/user.properties")) {
                properties = new Properties();
                properties.load(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    public void saveProperties() {
        if(properties == null) return;

        try {
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*

    This is where the transition from the ui to the scraper happens

     */

    public void startScrape(double minIntra, double maxIntra, double waitElement, boolean pauseAfterElement,
                            SimpleStringProperty logText, Boolean headless,
                            ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems) {

        if(scrapingService != null) {
            if(scrapingService.stateProperty().get() == Worker.State.RUNNING) return;
            else cancelScrape();
        }

        if(!updateDbConnection()) return;

        scrapingService = new WebsiteScraper(logText, headless, dbConnection, pauseAfterElement);
        // injecting the application context
        beanFactory.autowireBean(scrapingService);

        scrapingService.setMinIntraSiteDelay(minIntra);
        scrapingService.setMaxIntraSiteDelay(maxIntra);
        scrapingService.setWaitForWsElementSec(waitElement);
        scrapingService.resetTaskData(checkedItems);

        logText.set("");

        // dispatch
        scrapingService.start();
    }

    public void cancelScrape() {
        if(scrapingService != null) {
            scrapingService.cancel();
            // closing db connection inside
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

    public void bindProgressBars(ProgressBar websites, ProgressBar elements, ProgressBar selections, ProgressIndicator waitProgress) {
        if(scrapingService == null) return;

        // unidirectional wont let me reset the bars if done
        websites.progressProperty().bindBidirectional(scrapingService.websiteProgressProperty());
        elements.progressProperty().bindBidirectional(scrapingService.singleElementProgressProperty());
        selections.progressProperty().bindBidirectional(scrapingService.elementSelectionProgressProperty());
        waitProgress.progressProperty().bindBidirectional(scrapingService.waitProgressProperty());

    }
}
