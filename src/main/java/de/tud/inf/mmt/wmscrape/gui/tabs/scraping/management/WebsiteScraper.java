package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteConnection{


    private int minActionDelay = 2000;
    private int maxActionDelay = 5000;


    public WebsiteScraper(Website website, SimpleStringProperty logText, Boolean headless) {
        super(website, logText, headless);
    }


    public void setMinActionDelay(int minActionDelay) {
        this.minActionDelay = minActionDelay;
    }

    public void setMaxActionDelay(int maxActionDelay) {
        this.maxActionDelay = maxActionDelay;
    }

    protected boolean doLoginRoutine() {
        startBrowser();
        loadLoginPage();

        delayRandom();
        if(!acceptCookies()) return false;

        delayRandom();
        if(!hideCookies()) return false;

        delayRandom();
        if(!fillLoginInformation()) return false;

        delayRandom();
        return login();
    }

    private void delayRandom() {
        // from sec to milliseconds
        int randSleep = ThreadLocalRandom.current().nextInt(minActionDelay, maxActionDelay*1000 + 1);

        try {
            Thread.sleep(randSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processElement(WebsiteConnection connection, WebsiteElement element) {
        boolean loggedIn = doLoginRoutine();
        MultiplicityType multiplicityType = element.getMultiplicityType();
        ContentType contentType = element.getContentType();

        for(ElementSelection selection : element.getElementSelections()) {
            if(selection.isSelected()) {

                switch (multiplicityType) {
                    case TABELLE -> processTable();
                    case EINZELWERT -> {
                        switch (contentType) {
                            case STAMMDATEN -> System.out.println("1");
                            case WECHSELKURS -> System.out.println("2");
                            case AKTIENKURS -> processSingleCourse(selection, element.getElementIdentCorrelations());
                        }
                    }
                }

            }
        }
    }

    private void processSingleCourse(ElementSelection selection, List<ElementIdentCorrelation> correlations) {
        String isin = selection.getIsin();
        var dateCorr = getDateCorrelation(correlations);
        IdentType dateType = getSingleType(dateCorr);

        for(var correlation : correlations) {
            if(!correlation.equals(dateCorr)) {

            }

        }
    }


    private ElementIdentCorrelation getDateCorrelation(List<ElementIdentCorrelation> correlations) {
        for(var correlation : correlations) {
            if(correlation.getCourseDataDbTableColumn().getName().equals("datum")) {
                return correlation;
            }
        }
        return null;
    }

    private IdentType getSingleType(ElementIdentCorrelation correlation) {
        return IdentType.valueOf(correlation.getIdentType());
    }

    private void processTable() {}

}
