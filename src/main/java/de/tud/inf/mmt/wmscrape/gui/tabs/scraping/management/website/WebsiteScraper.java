package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElementRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteHandler {



    private long minIntraSiteDelay = 5000;
    private long maxIntraSiteDelay = 10000;
    private final Connection dbConnection;
    private final Extraction singleCourseOrStockExtraction;
    private final Extraction singleExchangeExtraction;
    private final Extraction tableExchangeExtraction;
    private final Extraction tableCourseOrStockExtraction;

    // only possible due to beanFactory.autowireBean(scrapingService);
    @Autowired
    ConfigurableApplicationContext context;
    @Autowired
    private WebsiteElementRepository repository;
    private Map<Website, List<WebsiteElement>> selectedFromTree;
    private Map<Website, Double> progressElementMax;
    private Map<Website, Double> progressElementCurrent;
    private boolean loggedInToWebsite = false;
    private double progressWsMax = 0.0001;
    private double progressWsCurrent = 0.0001;
    private boolean pauseAfterElement;

    public WebsiteScraper(SimpleStringProperty logText, Boolean headless, Connection dbConnection, boolean pauseAfterElement) {
        super(logText, headless);
        Date dateToday = Date.valueOf(LocalDate.now());
        singleCourseOrStockExtraction = new SingleCourseOrStockExtraction(dbConnection, logText, this, dateToday);
        singleExchangeExtraction = new SingleExchangeExtraction(dbConnection, logText, this, dateToday);
        tableExchangeExtraction = new TableExchangeExtraction(dbConnection, logText, this, dateToday);
        tableCourseOrStockExtraction = new TableCourseOrStockExtraction(dbConnection, logText, this, dateToday);
        this.dbConnection = dbConnection;
        this.pauseAfterElement = pauseAfterElement;
        selectedFromTree = new HashMap<>();
    }

    public void setMinIntraSiteDelay(double minIntraSiteDelay) {
        this.minIntraSiteDelay = (long) minIntraSiteDelay*1000;
    }

    public void setMaxIntraSiteDelay(double maxIntraSiteDelay) {
        this.maxIntraSiteDelay = (long) maxIntraSiteDelay*1000;
    }

    public void setPauseAfterElement(boolean pauseAfterElement) {
        this.pauseAfterElement = pauseAfterElement;
    }

    private boolean doLoginRoutine() {
        if(!usesLogin()) return true;
        if(!loadLoginPage()) return false;
        if(!acceptCookies()) return false;
        if(!hideCookies()) return false;
        if(!fillLoginInformation()) return false;

        delayRandom();
        if(!login()) return false;

        delayRandom();
        return true;
    }

    private boolean usesLogin() {
        return website.getUsernameIdentType() != IdentType.DEAKTIVIERT &&
                website.getPasswordIdentType() != IdentType.DEAKTIVIERT;
    }

    private void delayRandom() {
        long randTime = ThreadLocalRandom.current().nextLong(minIntraSiteDelay, maxIntraSiteDelay + 1);
        addToLog("INFO:\tWarte "+randTime+"ms");

        try {
            Thread.sleep(randTime);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void quit() {
        super.quit();
        try {
            if(!dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private WebsiteElement getFreshElement(WebsiteElement stale) {
        return repository.findById(stale.getId()).orElse(null);
    }

    private void processWebsiteElement(WebsiteElement element, MultiplicityType multiplicityType,
                                       ContentType contentType, Task<Void> task) {

        context.getBean(TransactionTemplate.class).execute(new TransactionCallbackWithoutResult() {
            // have to create a session by my own because this is an unmanaged object
            // otherwise no hibernate proxy is created
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                // have to re-fetch every element because the ones in the list have no proxy assigned anymore
                // should not be that worse considering the wait time between page loads
                WebsiteElement freshElement  = getFreshElement(element);

                switch (multiplicityType) {
                    case TABELLE -> {
                        switch (contentType) {
                            case AKTIENKURS, STAMMDATEN -> tableCourseOrStockExtraction.extract(freshElement, task);
                            case WECHSELKURS -> tableExchangeExtraction.extract(freshElement, task);
                        }
                    }
                    case EINZELWERT -> {
                        switch (contentType) {
                            case AKTIENKURS, STAMMDATEN -> singleCourseOrStockExtraction.extract(freshElement, task);
                            case WECHSELKURS -> singleExchangeExtraction.extract(freshElement, task);
                        }
                    }
                }
            }
        });
    }

    public String findText(IdentType type, String identifier, String highlightText) {
        return findTextInContext(type, identifier, highlightText, null);
    }

    public String findTextInContext(IdentType type, String identifier, String highlightText, WebElementInContext webElementInContext) {

        WebElement element;

        if(webElementInContext != null) {
            element = extractElementFromContext(type, identifier, webElementInContext);
        } else {
            element = extractElementFromRoot(type, identifier);
        }

        if(element == null) return "";

        // highlight after extraction otherwise the highlight text ist extracted too
        var tmp = element.getText().trim();
        if(!headless) highlightElement(element, highlightText);
        return tmp;
    }

    // has to be called while inside the frame
    public void highlightElement(WebElement element, String text) {
        if(headless || element == null) return;

        js.executeScript("arguments[0].setAttribute('style', 'border:2px solid #c95c55;')", element);

        if(text != null) {
            js.executeScript("var d = document.createElement('div');" +
                    "d.setAttribute('style','position:relative;display:inline-block;');" +
                    "var s = document.createElement('span');" +
                    "s.setAttribute('style','background-color:#c95c55;color:white;position:absolute;bottom:125%;left:50%;padding:0 5px;');" +
                    "var t = document.createTextNode('" + text + "');" +
                    "s.appendChild(t);" +
                    "d.appendChild(s);" +
                    "arguments[0].appendChild(d);", element);
        }
    }



    public void resetData(Map<Website, ObservableList<WebsiteElement>> selectedElements) {
        // making a shallow copy to not touch the treeView
        Map<Website, List<WebsiteElement>> dereferenced = new HashMap<>();
        progressElementMax = new HashMap<>();
        progressElementCurrent = new HashMap<>();

        for(var key : selectedElements.keySet()) {
            dereferenced.put(key, new ArrayList<>(selectedElements.get(key)));
            progressElementCurrent.put(key, 0.0);
            progressElementMax.put(key, (double) selectedElements.get(key).size());
        }
        selectedFromTree = dereferenced;

        website = null;
        loggedInToWebsite = false;

        progressWsMax = selectedFromTree.size()+0.0001;
        progressWsCurrent = 0.0001;
    }

    // if not set get any
    public void updateWebsite() {
        if (website == null && selectedFromTree != null && selectedFromTree.keySet().iterator().hasNext()) {
            // make sure to remove used website keys later

            if(loggedInToWebsite) logout();
            website = selectedFromTree.keySet().iterator().next();
            loggedInToWebsite = false;
        }
    }

    public Website getWebsite() {
        updateWebsite();
        return website;
    }

    public WebsiteElement getElement() {
        if(website == null) {
            website = getWebsite();
        }

        if(website == null || !selectedFromTree.containsKey(website) || selectedFromTree.get(website).isEmpty()) {
            return null;
        }

        return selectedFromTree.get(website).get(0);
    }

    public void removeFinishedWebsite(Website finishedWs) {
        if(selectedFromTree != null) {
            selectedFromTree.remove(finishedWs);
        }

        if(loggedInToWebsite && finishedWs != null) logout();
        loggedInToWebsite = false;
        website = null;
    }

    public void removeFinishedElement(Website website, WebsiteElement element) {
        if(selectedFromTree == null || !selectedFromTree.containsKey(website)) return;

        selectedFromTree.get(website).remove(element);
        if(selectedFromTree.get(website).isEmpty()) {
            removeFinishedWebsite(website);
        }
    }

    private final SimpleDoubleProperty singleElementProgress = new SimpleDoubleProperty(0);
    private final SimpleDoubleProperty elementSelectionProgress = new SimpleDoubleProperty(0);

    @Override
    public Task<Void> createTask() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {

                if(selectedFromTree == null || selectedFromTree.isEmpty()) {
                    quit();
                    return null;
                }

                updateWebsite();

                // no website left -> done
                while (website != null && !this.isCancelled()) {

                    // do log in routine
                    // check everytime due to the resume option
                    if (!loggedInToWebsite || !browserIsOpen()) {
                        // error on starting the browser
                        if (!startBrowser()) {
                            removeFinishedWebsite(website);
                            updateWebsite();
                            updateProgress(progressWsCurrent / progressWsMax, 1);
                            progressWsCurrent++;
                            continue;
                        }
                        // returns false if error at login
                        if (!doLoginRoutine()) {
                            addToLog("ERR:\t\tLogin nicht korrekt durchgeführt. Abbruch der Bearbeitung.");
                            removeFinishedWebsite(website);
                            updateWebsite();
                            updateProgress(progressWsCurrent / progressWsMax, 1);
                            progressWsCurrent++;
                            continue;
                        }
                        loggedInToWebsite = true;

                        // get next element and extract
                    }

                    if(!selectedFromTree.containsKey(website)) {
                        updateWebsite();
                        continue;
                    }

                    double currentElementProgress = progressElementMax.get(website);
                    double maxElementProgress = progressElementCurrent.get(website);

                    singleElementProgress.set(currentElementProgress);

                    WebsiteElement element = getElement();
                    while (element != null && !this.isCancelled()) {

                        if (element.getWebsite() == null || element.getInformationUrl() == null || element.getInformationUrl().isBlank()) {
                            addToLog("ERR:\t\tKeine Webseite oder URl angegeben für: " + element.getDescription());
                            removeFinishedElement(website, element);
                            element = getElement();
                            singleElementProgress.set(currentElementProgress/maxElementProgress);
                            progressElementCurrent.put(website, progressElementMax.get(website)+ 1);
                            continue;
                        } if (!loadPage(element.getInformationUrl())) {
                            addToLog("ERR:\t\tErfolgloser Zugriff " + element.getInformationUrl());
                            removeFinishedElement(website, element);
                            element = getElement();
                            singleElementProgress.set(currentElementProgress/maxElementProgress);
                            progressElementCurrent.put(website, progressElementMax.get(website)+ 1);
                            continue;
                        }
                        addToLog("\n");

                        // the main action does happen here
                        processWebsiteElement(element, element.getMultiplicityType(), element.getContentType(), this);


                        addToLog("\n");
                        delayRandom();

                        // takes care of the websites too
                        removeFinishedElement(website, element);
                        element = getElement();
                        progressElementCurrent.put(website, progressElementMax.get(website)+ 1);
                        singleElementProgress.set(currentElementProgress/maxElementProgress);

                        // stop execution
                        if (pauseAfterElement) return null;//this.cancel();//return null;
                    }

                    removeFinishedWebsite(website);
                    updateWebsite();
                    updateProgress(progressWsCurrent / progressWsMax, 1);
                    progressWsCurrent++;
                }

                if(selectedFromTree.size() == 0) quit();

                return null;
            }
        };

        this.exceptionProperty().addListener((o, ov, nv) ->  {
            if(nv != null) {
                Exception e = (Exception) nv;
                System.out.println(e.getMessage()+"  "+e.getCause());
            }
        });

        return task;
    }

    @Override
    public void start() {
        System.out.println("start");
        super.start();

    }

    @Override
    public void restart() {
        System.out.println("restart");
        super.restart();
    }

    @Override
    public boolean cancel() {
        super.cancel();
        System.out.println("cancel");
        return false;
    }

    @Override protected void succeeded() {
        super.succeeded();
        System.out.println("success");
    }
}
