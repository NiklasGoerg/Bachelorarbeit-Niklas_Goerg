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
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteHandler {

    // only possible due to beanFactory.autowireBean(scrapingService);
    @Autowired
    ConfigurableApplicationContext context;
    @Autowired
    private WebsiteElementRepository repository;

    private double minIntraSiteDelay = 4000;
    private double maxIntraSiteDelay = 6000;
    private final Connection dbConnection;
    private final Extraction singleCourseOrStockExtraction;
    private final Extraction singleExchangeExtraction;
    private final Extraction tableExchangeExtraction;
    private final Extraction tableCourseOrStockExtraction;

    private Map<Website, List<WebsiteElement>> selectedFromMenuTree;
    private Map<Website, Double> progressElementMax;
    private Map<Website, Double> progressElementCurrent;
    private boolean loggedInToWebsite = false;
    private double progressWsMax = 0.0001;
    private double progressWsCurrent = 0;
    private boolean pauseAfterElement;

    // using my own progress for the main task because the updateProgress method lags the ui
    private final SimpleDoubleProperty websiteProgress = new SimpleDoubleProperty(0);
    private final SimpleDoubleProperty singleElementProgress = new SimpleDoubleProperty(0);
    private final SimpleDoubleProperty elementSelectionProgress = new SimpleDoubleProperty(0);
    private final SimpleDoubleProperty waitProgress = new SimpleDoubleProperty(0);

    private final HashMap<String, String> identBuffer = new HashMap<>();

    public WebsiteScraper(SimpleStringProperty logText, Boolean headless, Connection dbConnection, boolean pauseAfterElement) {
        super(logText, headless);
        Date dateToday = Date.valueOf(LocalDate.now());
        singleCourseOrStockExtraction = new SingleCourseOrStockExtraction(dbConnection, logText, this, dateToday);
        singleExchangeExtraction = new SingleExchangeExtraction(dbConnection, logText, this, dateToday);
        tableExchangeExtraction = new TableExchangeExtraction(dbConnection, logText, this, dateToday);
        tableCourseOrStockExtraction = new TableCourseOrStockExtraction(dbConnection, logText, this, dateToday);
        this.dbConnection = dbConnection;
        this.pauseAfterElement = pauseAfterElement;
        selectedFromMenuTree = new HashMap<>();
    }

    public void setMinIntraSiteDelay(double minIntraSiteDelay) {
        this.minIntraSiteDelay = minIntraSiteDelay*1000;
    }

    public void setMaxIntraSiteDelay(double maxIntraSiteDelay) {
        this.maxIntraSiteDelay = maxIntraSiteDelay*1000;
    }

    public void setPauseAfterElement(boolean pauseAfterElement) {
        this.pauseAfterElement = pauseAfterElement;
    }

    public SimpleDoubleProperty websiteProgressProperty() {
        return websiteProgress;
    }

    public SimpleDoubleProperty singleElementProgressProperty() {
        return singleElementProgress;
    }

    public SimpleDoubleProperty elementSelectionProgressProperty() {
        return elementSelectionProgress;
    }

    public SimpleDoubleProperty waitProgressProperty() {
        return waitProgress;
    }

    private boolean doLoginRoutine() {
        if(!usesLogin()) return true;
        if(!loadLoginPage()) return false;
        delayRandom();
        if(!acceptCookies()) return false;
        delayRandom();
        if(!fillLoginInformation()) return false;
        if(!login()) return false;
        delayRandom();
        return true;
    }

    private boolean usesLogin() {
        return website.getUsernameIdentType() != IdentType.DEAKTIVIERT &&
                website.getPasswordIdentType() != IdentType.DEAKTIVIERT;
    }

    private void delayRandom() {
        double randTime = ThreadLocalRandom.current().nextDouble(minIntraSiteDelay, maxIntraSiteDelay + 1);
        addToLog("INFO:\tWarte "+(Math.round((randTime/1000)*100.0)/100.0)+"s");

        double i=0;
        try {
            while (i* 300 < randTime) {
                i++;
                Thread.sleep(300L);
                waitProgress.set((i*300)/randTime);
            }
        } catch (InterruptedException e) {
            // catch threat interrupt
        }
        waitProgress.setValue(0);
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

    public String findTextInContext(IdentType type, String identifier, String highlightText,
                                    WebElementInContext webElementInContext) {

        // if already visited return
        // mainly done to not mark elements again
        String buffered = identBuffer.get(identifier);
        if(buffered != null) return buffered;

        WebElement element;

        // null -> search in root frame
        if(webElementInContext != null) {
            element = extractElementFromContext(type, identifier, webElementInContext);
        } else {
            element = extractElementFromRoot(type, identifier);
        }

        if(element == null) return "";

        // highlight after extraction otherwise the highlight text ist extracted too
        var tmp = element.getText().trim();

        identBuffer.put(identifier, tmp);
        if(!headless) highlightElement(element, highlightText);
        return tmp;
    }

    public void resetIdentBuffer() {
        identBuffer.clear();
    }

    // has to be called while inside the frame
    public void highlightElement(WebElement element, String text) {
        if(headless || element == null) return;

        try {
            driver.executeScript("arguments[0].setAttribute('style', 'border:2px solid #c95c55;')", element);

            if (text != null) {
                driver.executeScript("var d = document.createElement('div');" +
                        "d.setAttribute('style','position:relative;display:inline-block;');" +
                        "var s = document.createElement('span');" +
                        "s.setAttribute('style','background-color:#c95c55;color:white;position:absolute;bottom:125%;left:50%;padding:0 5px;');" +
                        "var t = document.createTextNode('" + text + "');" +
                        "s.appendChild(t);" +
                        "d.appendChild(s);" +
                        "arguments[0].appendChild(d);", element);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage()+" "+ e.getCause());
        }
    }

    public void resetTaskData(Map<Website, ObservableList<WebsiteElement>> selectedElements) {
        // making a shallow copy to not touch the treeView
        Map<Website, List<WebsiteElement>> dereferenced = new HashMap<>();
        // resetting progress
        progressElementMax = new HashMap<>();
        progressElementCurrent = new HashMap<>();

        for(var key : selectedElements.keySet()) {
            dereferenced.put(key, new ArrayList<>(selectedElements.get(key)));
            progressElementCurrent.put(key, 0.0);
            progressElementMax.put(key, (double) selectedElements.get(key).size());
        }

        selectedFromMenuTree = dereferenced;

        website = null;
        loggedInToWebsite = false;

        progressWsMax = selectedFromMenuTree.size();
        progressWsCurrent = 0;
        websiteProgress.set(0);
        singleElementProgress.set(0);
        elementSelectionProgress.set(0);
    }

    private WebsiteElement getFreshElement(WebsiteElement stale) {
        return repository.findById(stale.getId()).orElse(null);
    }

    // if not set get any
    public void updateWebsite() {
        if (website == null && selectedFromMenuTree != null && selectedFromMenuTree.keySet().iterator().hasNext()) {
            loggedInToWebsite = false;
            website = selectedFromMenuTree.keySet().iterator().next();
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

        if(website == null || !selectedFromMenuTree.containsKey(website) || selectedFromMenuTree.get(website).isEmpty()) {
            return null;
        }

        return selectedFromMenuTree.get(website).get(0);
    }

    public void removeFinishedWebsite() {
        if(selectedFromMenuTree != null) {
            selectedFromMenuTree.remove(website);
        }

        if(loggedInToWebsite && website != null && usesLogin()) {
            logout();
            delayRandom();
        }

        loggedInToWebsite = false;
        website = null;
        progressWsCurrent++;
    }

    public void removeFinishedElement(WebsiteElement element) {
        if(selectedFromMenuTree == null || !selectedFromMenuTree.containsKey(website)) return;

        var selected = selectedFromMenuTree.getOrDefault(website, null);
        if(selected != null) {
            selected.remove(element);
            // don't delay at the last element after which the logout delay occours
            if(selected.size()>0) delayRandom();
        }

        progressElementCurrent.put(website, progressElementCurrent.get(website)+ 1);
    }

    @Override
    public Task<Void> createTask() {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {

                if(isEmptyTask()) return null;

                updateWebsite();

                while (website != null && !this.isCancelled()) {

                    // do log in routine
                    // check everytime due to the pause/resume option
                    if (!loggedInToWebsite || !browserIsOpen()) {
                        if (!startBrowser()) return null;

                        // returns false if error at login
                        if (logInError()) {
                            websiteProgress.set(progressWsCurrent / progressWsMax);
                            if (isPauseAfterElement()) return null;
                            else continue;
                        }
                        loggedInToWebsite = true;
                    }

                    double maxElementProgress = progressElementMax.get(website);
                    double currentElementProgress = progressElementCurrent.get(website);
                    singleElementProgress.set(currentElementProgress/maxElementProgress);

                    WebsiteElement element = getElement();

                    while (element != null && !this.isCancelled()) {

                        if (missingWebsiteSettings(element) || noPageLoadSuccess(element)) {
                            element = getElement();
                            singleElementProgress.set(currentElementProgress/maxElementProgress);
                            continue;
                        }

                        // the main action does happen here
                        processWebsiteElement(element, element.getMultiplicityType(), element.getContentType(), this);

                        removeFinishedElement(element);
                        singleElementProgress.set(progressElementCurrent.get(website)/maxElementProgress);
                        element = getElement();

                        // stop execution
                        if (isPauseAfterElement()) return null;
                    }

                    removeFinishedWebsite();
                    updateWebsite();
                    websiteProgress.set(progressWsCurrent / progressWsMax);
                }

                if(selectedFromMenuTree.isEmpty()) quit();
                return null;
            }
        };

        addExceptionListener(task);

        return task;
    }

    private boolean isPauseAfterElement() {
        if (pauseAfterElement) {
            addToLog("INFO:\tVorgang pausiert. Weiter klicken zum Fortfahren.");
            return true;
        }
        return false;
    }

    private boolean isEmptyTask() {
        if(selectedFromMenuTree == null || selectedFromMenuTree.isEmpty()) {
            addToLog("INFO:\tKeine Elemente zum Scrapen ausgewählt.");
            quit();
            return true;
        }
        return false;
    }

    private boolean missingWebsiteSettings(WebsiteElement element) {
        if (element.getWebsite() == null || element.getInformationUrl() == null || element.getInformationUrl().isBlank()) {
            addToLog("ERR:\t\tKeine Webseite oder URl angegeben für " + element.getDescription());
            removeFinishedElement(element);
            updateWebsite();
            return true;
        }
        return false;
    }

    private boolean noPageLoadSuccess(WebsiteElement element) {
        // loading page here
        if (!loadPage(element.getInformationUrl())) {
            addToLog("ERR:\t\tErfolgloser Zugriff auf " + element.getInformationUrl());
            removeFinishedElement(element);
            return true;
        }
        return false;
    }

    private boolean logInError() {
        if(!doLoginRoutine()) {
            addToLog("ERR:\t\tLogin nicht korrekt durchgeführt für " + website.getUrl());
            removeFinishedWebsite();
            return true;
        }
        return false;
    }

    private void addExceptionListener(Task<Void> task) {
        task.exceptionProperty().addListener((o, ov, nv) ->  {
            if(nv != null) {
                Exception e = (Exception) nv;
                System.out.println(e.getMessage()+" "+e.getCause());
            }
        });
    }

    private void processWebsiteElement(WebsiteElement element, MultiplicityType multiplicityType,
                                       ContentType contentType, Task<Void> task) {

        context.getBean(TransactionTemplate.class).execute(new TransactionCallbackWithoutResult() {
            // have to create a session by my own because this is an unmanaged object
            // otherwise no hibernate proxy is created
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {

                // have to re-fetch the element because the ones in the list have no proxy assigned.
                // should not be that bad considering the wait time between page loads
                WebsiteElement freshElement  = getFreshElement(element);

                switch (multiplicityType) {
                    case TABELLE -> {
                        switch (contentType) {
                            case AKTIENKURS, STAMMDATEN -> tableCourseOrStockExtraction.extract(
                                    freshElement, task, elementSelectionProgress);

                            case WECHSELKURS -> tableExchangeExtraction.extract(
                                    freshElement, task, elementSelectionProgress);
                        }
                    }
                    case EINZELWERT -> {
                        switch (contentType) {
                            case AKTIENKURS, STAMMDATEN -> singleCourseOrStockExtraction.extract(
                                    freshElement, task, elementSelectionProgress);

                            case WECHSELKURS -> singleExchangeExtraction.extract(
                                    freshElement, task, elementSelectionProgress);
                        }
                    }
                }
            }
        });
    }
}
