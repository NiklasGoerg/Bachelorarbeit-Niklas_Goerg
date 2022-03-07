package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebRepresentation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteTree;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.concurrent.Worker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeView;
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
import java.util.Properties;
import java.util.Set;

@Service
public class ScrapingManager {

    @Autowired private AutowireCapableBeanFactory beanFactory;
    @Autowired private DataSource dataSource;
    @Autowired private WebsiteManager websiteManager;
    private WebsiteScraper scrapingService;
    private Connection dbConnection;
    private Properties properties;

    /**
     * creates the selection tree for the scraping menu
     *
     * @param checkedItems the list that holds the selected elements
     * @param restored the hash values of the previously selected elements
     * @return the javafx tree view
     */
    @Transactional
    public TreeView<WebRepresentation<?>> createSelectionTree(
            ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems, Set<Integer> restored) {
        var websites = websiteManager.getWebsites(false);
        return (new WebsiteTree(websites, checkedItems, restored)).getTreeView();
    }

    /**
     * loads the properties from user.properties
     * @return the properties object
     */
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

    /**
     * stores the properties file
     */
    public void saveProperties() {
        if(properties == null) return;

        try {
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * takes all the parameters from the scraping menu and starts the scraping task if none is running
     *
     * @param minIntra minimum wait time before the next page load can be done
     * @param maxIntra maximum wait time before the next page load can be done
     * @param waitElement wait time for website elements
     * @param pauseAfterElement if true the task pauses after every element
     * @param logText the text property of the log field
     * @param headless if true no browser window will be shown
     * @param checkedItems the selected items from the selection tree
     */
    public void startScrape(double minIntra, double maxIntra, double waitElement, boolean pauseAfterElement,
                            SimpleStringProperty logText, Boolean headless,
                            ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems) {

        if(checkedItems.isEmpty() || emptyMapValues(checkedItems)) {
            logText.set("\nINFO:\tEs wurden keine Elemente zum scrapen ausgewählt.");
            return;
        }

        if(scrapingService != null) {
            if(scrapingService.stateProperty().get() == Worker.State.RUNNING ||
                    scrapingService.stateProperty().get() == Worker.State.SCHEDULED) return;
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

    /**
     * forces the task to stop
     */
    public void cancelScrape() {
        if(scrapingService != null) {
            scrapingService.cancel();
            // closing db connection inside
            scrapingService.quit();
            scrapingService = null;
        }
    }

    /**
     * continues the scraping process after pausing it. allows the change of paramters
     *
     * @param minIntra minimum wait time before the next page load can be done
     * @param maxIntra maximum wait time before the next page load can be done
     * @param waitElement wait time for website elements
     * @param pauseAfterElement if true the task pauses after every element
     */
    public void continueScrape(double minIntra, double maxIntra, double waitElement, boolean pauseAfterElement) {

        if(scrapingService != null) {
            if (scrapingService.stateProperty().get() == Worker.State.SUCCEEDED) {
                scrapingService.setMinIntraSiteDelay(minIntra);
                scrapingService.setMaxIntraSiteDelay(maxIntra);
                scrapingService.setWaitForWsElementSec(waitElement);
                scrapingService.setPauseAfterElement(pauseAfterElement);
                // continues where it stopped
                scrapingService.restart();
            } else if(scrapingService.stateProperty().get() != Worker.State.RUNNING) {
                cancelScrape();
            }
        }
    }

    /**
     * sets the jdbc connection that will be used
     *
     * @return true if a connection has been set
     */
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

    /**
     * binds the javafx progress bar progress properties to the progress properties of the task.
     *
     * @param websites website progressbar
     * @param elements website element progressbar
     * @param selections selection / table row progressbar
     * @param waitProgress the clock like wait time progress bar (shows the intra site delay)
     */
    public void bindProgressBars(ProgressBar websites, ProgressBar elements, ProgressBar selections, ProgressIndicator waitProgress) {
        if(scrapingService == null) return;

        // unidirectional wont let me reset the bars if done
        websites.progressProperty().bindBidirectional(scrapingService.websiteProgressProperty());
        elements.progressProperty().bindBidirectional(scrapingService.singleElementProgressProperty());
        selections.progressProperty().bindBidirectional(scrapingService.elementSelectionProgressProperty());
        waitProgress.progressProperty().bindBidirectional(scrapingService.waitProgressProperty());
    }

    public static boolean emptyMapValues(ObservableMap<Website, ObservableSet<WebsiteElement>> map) {
        for (var list : map.values()) {
            if (!list.isEmpty()) return false;
        }
        return true;
    }
}
