package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction.SingleCourseOrStockExtraction;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction.SingleExchangeExtraction;
import javafx.beans.property.SimpleStringProperty;
import org.openqa.selenium.WebElement;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class WebsiteScraper extends WebsiteHandler {

    private int minIntraSiteDelay = 0;
    private int maxIntraSiteDelay = 2;
    private final Date dateToday = Date.valueOf(LocalDate.now());
    private final Connection connection;
    private final SingleCourseOrStockExtraction singleCourseOrStockExtraction;
    private final SingleExchangeExtraction singleExchangeExtraction;

    public WebsiteScraper(Website website, SimpleStringProperty logText, Boolean headless, Connection connection) {
        super(website, logText, headless);

        this.connection = connection;
        singleCourseOrStockExtraction = new SingleCourseOrStockExtraction(connection, logText, this, dateToday);
        singleExchangeExtraction = new SingleExchangeExtraction(connection, logText, this, dateToday);
    }

    public void setMinIntraSiteDelay(int minIntraSiteDelay) {
        this.minIntraSiteDelay = minIntraSiteDelay;
    }

    public void setMaxIntraSiteDelay(int maxIntraSiteDelay) {
        this.maxIntraSiteDelay = maxIntraSiteDelay;
    }

    private boolean doLoginRoutine() {
        loadLoginPage();

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
        int randTime = ThreadLocalRandom.current().nextInt(minIntraSiteDelay, maxIntraSiteDelay + 1);
        addToLog("INFO: Warte "+randTime+"ms");

        try {
            Thread.sleep(randTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processWebsite() {
        startBrowser();

        if(usesLogin()) {
            // returns false if error at login
            if (!doLoginRoutine()) {
                addToLog("FEHLER: Login nicht korrekt durchgeführt. Abbruch der Bearbeitung.");
                return;
            }
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

    @Override
    public void quit() {
        super.quit();
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
                    case AKTIENKURS,STAMMDATEN -> singleCourseOrStockExtraction.extract(element);
                    case WECHSELKURS -> singleExchangeExtraction.extract(element);
                }
            }
        }
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

        if(!headless) highlightElement(element, highlightText);

        return element.getText().trim();
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
}
