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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteConnection {

    private int minIntraSiteDelay = 0;
    private int maxIntraSiteDelay = 2;
    private final Date dataToday = Date.valueOf(LocalDate.now());
    private final SingleCourseElementExtraction singleCourseExtraction;
    private final Connection connection;

    public WebsiteScraper(Website website, SimpleStringProperty logText, Boolean headless, Connection connection) {
        super(website, logText, headless);

        this.connection = connection;
        singleCourseExtraction = new SingleCourseElementExtraction(connection, logText);
    }


    public void setMinIntraSiteDelay(int minIntraSiteDelay) {
        this.minIntraSiteDelay = minIntraSiteDelay;
    }

    public void setMaxIntraSiteDelay(int maxIntraSiteDelay) {
        this.maxIntraSiteDelay = maxIntraSiteDelay;
    }

    private boolean doLoginRoutine() {
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

    private boolean usesLogin() {
        return website.getUsernameIdentType() != IdentType.DEAKTIVIERT &&
                website.getPasswordIdentType() != IdentType.DEAKTIVIERT;
    }

    private void delayRandom() {
        // TODO log delay
        int randSleep = ThreadLocalRandom.current().nextInt(minIntraSiteDelay, maxIntraSiteDelay + 1);

        try {
            Thread.sleep(randSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO
    public void processWebsite() {
        startBrowser();

        // TODO log not logged in
        if(usesLogin()) {
            // returns false if error at login
            if (!doLoginRoutine()) return;
            // wait until page of first element is loaded
            delayRandom();
        }

        for(WebsiteElement element : website.getWebsiteElements()) {
            loadPage(element.getInformationUrl());

            var multiplicityType = element.getMultiplicityType();
            var contentType = element.getContentType();

            processWebsiteElement(element, multiplicityType, contentType);

            delayRandom();
        }
        logout();
    }

    public void quit() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processWebsiteElement(WebsiteElement element, MultiplicityType multiplicityType, ContentType contentType) {
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

        @Override
        protected PreparedStatement prepareStatement(Connection connection, PreparedCorrelation correlation) {
            String dbColName = correlation.getDbColName();

            String sql = "INSERT INTO "+correlation.getDbTableName()+" (" + dbColName + ", isin, datum) VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                    dbColName + "=VALUES(" + dbColName + ");";

            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(2, correlation.getIsin());
                statement.setDate(3, correlation.getDate());
                return statement;

            } catch (SQLException e) {
                e.printStackTrace();
                log("FEHLER: SQL-Statement Erstellung. Spalte '"+dbColName+"' der Tabelle "+correlation.getDbColName()
                        +". "+e.getMessage()+" <-> "+e.getCause());
            }
            return null;
        }

    }
}
