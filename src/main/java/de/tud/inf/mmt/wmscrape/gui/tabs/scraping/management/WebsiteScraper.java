package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteConnection {


    private int minActionDelay = 2000;
    private int maxActionDelay = 5000;
    private final Date dataToday = Date.valueOf(LocalDate.now());

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

        if(usesNoLogin()) return true;

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

    private boolean usesNoLogin() {
        if(website.getUsernameIdentType() == IdentType.DEAKTIVIERT ||
                website.getPasswordIdentType() == IdentType.DEAKTIVIERT ||
                website.getLogoutIdentType() == IdentType.DEAKTIVIERT) {
            return true;
        }
        return false;
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

    private void processElement(WebsiteElement element) {
        boolean loggedIn = doLoginRoutine();
        if(!loggedIn) return;

        delayRandom();
        loadPage(element.getInformationUrl());



        MultiplicityType multiplicityType = element.getMultiplicityType();
        ContentType contentType = element.getContentType();

        for(ElementSelection selection : element.getElementSelections()) {
            if(selection.isSelected()) {

                switch (multiplicityType) {
                    case TABELLE -> System.out.println("0");
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

        List<CorrelationResult> preparedCorrelationResults = prepareCorrelationResults(correlations);

        for(CorrelationResult result : preparedCorrelationResults) {
            extractCorrelationResult(result);
        }

    }

    private List<CorrelationResult> prepareCorrelationResults(List<ElementIdentCorrelation> correlations) {
        List<CorrelationResult> preparedCorrelationResults = new ArrayList<>();

        String colName;
        for(var correlation : correlations) {
            colName = correlation.getCourseDataDbTableColumn().getName();
            ColumnDatatype datatype = CourseDataDbManager.colNameToDataType.get(colName);
            preparedCorrelationResults.add(new CorrelationResult(colName, CourseDataDbManager.TABLE_NAME, dataToday, datatype, correlation.getIdentType()));
        }
        return preparedCorrelationResults;
    }

    private void extractCorrelationResult(CorrelationResult result) {
        String extract = extractTextDataByType(result.getIdentType(), result.getIdentifier());
        result.setWebsiteData(extract);

    }
}
