package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.scraping.PreparedCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.scraping.SingleElementExtraction;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteConnection {

    private int minIntraSiteDelay = 2000;
    private int maxIntraSiteDelay = 5000;
    private final Date dataToday = Date.valueOf(LocalDate.now());

    private final SingleCourseElementExtraction singleCourseExtraction;

    public WebsiteScraper(Website website, SimpleStringProperty logText, Boolean headless, Connection connection) {
        super(website, logText, headless);

        singleCourseExtraction = new SingleCourseElementExtraction(connection, logText);
    }


    public void setMinIntraSiteDelay(int minIntraSiteDelay) {
        this.minIntraSiteDelay = minIntraSiteDelay;
    }

    public void setMaxIntraSiteDelay(int maxIntraSiteDelay) {
        this.maxIntraSiteDelay = maxIntraSiteDelay;
    }

    private boolean doLoginRoutine() {
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
        if(!login()) return false;

        delayRandom();
        return true;
    }

    private boolean usesNoLogin() {
        return website.getUsernameIdentType() == IdentType.DEAKTIVIERT ||
                website.getPasswordIdentType() == IdentType.DEAKTIVIERT ||
                website.getLogoutIdentType() == IdentType.DEAKTIVIERT;
    }

    private void delayRandom() {
        int randSleep = ThreadLocalRandom.current().nextInt(minIntraSiteDelay, maxIntraSiteDelay + 1);

        try {
            Thread.sleep(randSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO
    public void processElement(WebsiteElement element) {
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
                            case AKTIENKURS -> singleCourseExtraction.extract(element);
                        }
                    }
                }

            }
        }
    }

    private class SingleCourseElementExtraction extends SingleElementExtraction {

        protected SingleCourseElementExtraction(Connection connection, SimpleStringProperty logText) {
            super(connection, logText, CourseDataDbManager.TABLE_NAME);
        }

        @Override
        protected PreparedCorrelation prepareCorrelation(ElementIdentCorrelation correlation, ElementSelection selection) {
            String colName = correlation.getCourseDataDbTableColumn().getName();
            String tableName = correlation.getCourseDataDbTableColumn().getTableName();
            ColumnDatatype datatype = correlation.getColumnDatatype();

            var preparedCorrelation = new PreparedCorrelation(tableName, colName, dataToday, datatype, correlation.getIdentType(), correlation.getIdentification());
            preparedCorrelation.setIsin(selection.getIsin());
            return preparedCorrelation;
        }

        @Override
        protected String findData(PreparedCorrelation correlation) {
            // the reason this is an embedded class. to access the method
            return findTextDataByType(correlation.getIdentType(), correlation.getIdentifier(), correlation.getDbColName());
        }

    }
}
