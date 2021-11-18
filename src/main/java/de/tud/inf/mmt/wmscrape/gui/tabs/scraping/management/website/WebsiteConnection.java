package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.beans.property.SimpleStringProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class WebsiteConnection {

    public static final int IFRAME_SEARCH_DEPTH = 3;
    protected final Website website;
    private int waitForWsElementSec = 5;
    private final boolean headless;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private SimpleStringProperty logText;

    public WebsiteConnection(Website website) {
        this.website = website;
        headless = true;
        this.logText = new SimpleStringProperty();
    }

    public WebsiteConnection(Website website, SimpleStringProperty logText, Boolean headless) {
        this.website = website;
        this.headless = headless;
        this.logText = logText;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setWaitForWsElementSec(int waitForWsElementSec) {
        this.waitForWsElementSec = waitForWsElementSec;
    }

    protected WebDriver getDriver() {
        return driver;
    }

    protected void startBrowser() {
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(firefoxBinary);
        options.setLogLevel(FirefoxDriverLogLevel.ERROR);

        if(headless) options.setHeadless(true);

        driver = new FirefoxDriver(options);
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(waitForWsElementSec));
    }

    protected void loadLoginPage() {
        loadPage(website.getUrl());
    }

    protected boolean acceptCookies() {
        IdentType type = website.getCookieAcceptIdentType();
        if(type == IdentType.DEAKTIVIERT) return true;

        WebElement element = findElementByType(type, website.getCookieAcceptIdent());

        if(element == null) return false;

        clickElement(element);
        addToLog("INFO: Cookies akzeptiert");
        return true;
    }

    protected boolean hideCookies() {
        IdentType type = website.getCookieHideIdentType();
        if(type == IdentType.DEAKTIVIERT) return true;

        String identifier = website.getCookieHideIdent().replace("\"","'");


        WebElement element= findElementByType(type, identifier);
        if(element != null) {
            js.executeScript("(arguments[0]).remove()", element);
        } else {
            addToLog("WARNUNG: Cookiebanner nicht ausgeblendet");
            return false;
        }

        addToLog("INFO: Cookiebanner ausgeblendet");
        return true;
    }

    protected boolean fillLoginInformation() {

        WebElement username = findElementByType(website.getUsernameIdentType(), website.getUsernameIdent());
        if(username == null) return false;
        setText(username, website.getUsername());

        WebElement password = findElementByType(website.getPasswordIdentType(), website.getPasswordIdent());
        if(password == null) return false;
        setText(password, website.getPassword());

        addToLog("INFO: Login Informationen ausgefüllt");
        return true;
    }

    protected boolean login() {
        WebElement password = findElementByType(website.getPasswordIdentType(), website.getPasswordIdent());

        // submit like pressing enter
        if(password != null && website.getLoginButtonIdentType() == IdentType.ENTER) {
            submit(password);
            return true;
        }

        WebElement loginButton = findElementByType(website.getLoginButtonIdentType(), website.getLoginButtonIdent());
        if(loginButton == null) return false;
        clickElement(loginButton);

        addToLog("INFO: Login erfolgreich");
        return true;
    }

    protected boolean logout() {
        IdentType type = website.getLogoutIdentType();
        if(type == IdentType.DEAKTIVIERT) return true;

        // // called separately in tester
        if(website.getCookieHideIdentType() != IdentType.DEAKTIVIERT
                && !(this instanceof WebsiteTester)) hideCookies();

        if(type == IdentType.URL) {
            driver.get(website.getLogoutIdent());
            addToLog("INFO: Logout erfolgreich");
            return true;
        }

        WebElement logoutButton = findElementByType(type, website.getLogoutIdent());
        if(logoutButton == null) return false;
        clickElement(logoutButton);

        addToLog("INFO: Logout erfolgreich");
        return true;
    }

    protected void loadPage(String url) {
        driver.get(url);
        waitLoadEvent();
        addToLog("INFO: "+website.getUrl()+" geladen");
    }

    private void waitLoadEvent() {
        wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
    }

    private WebElement findElementByType(IdentType type, String identifier) {
        // called separately in tester
        if(website.getCookieHideIdentType() != IdentType.DEAKTIVIERT
                && !(this instanceof WebsiteTester)) hideCookies();

        // search element in main document
        WebElement element = extractElementsByType(type, identifier);
        if(element != null) return element;

        // seach in sub iframes
        element = recursiveSearch(type, identifier, driver.findElements(By.tagName("iframe")), 0);
        if(element != null) {
            return element;
        }

        addToLog("FEHLER: Kein Element unter '"+identifier+"' gefunden");
        return null;
    }

    private WebElement extractElementsByType(IdentType type, String identifier) {
        List<WebElement> elements;

        try {
            if (type == IdentType.ID) {
                elements = driver.findElements(By.id(identifier));
            } else if (type == IdentType.XPATH) {
                elements = driver.findElements(By.xpath(identifier));
            } else if (type == IdentType.CSS) {
                elements = driver.findElements(By.cssSelector(identifier));
            } else return null;
        } catch (InvalidSelectorException e) {
            return null;
        }

        if(elements.size()>0) return elements.get(0);
        return null;
    }

    protected String findTextDataByType(IdentType type, String identifier, String highlightText) {
        WebElement element = findElementByType(type, identifier);
        if(element == null) return null;
        String text = element.getText();
        if(!headless) highlightElement(element, highlightText);
        driver.switchTo().defaultContent();
        return text;
    }

    private void setText(WebElement element, String text) {
        element.sendKeys(text);
        driver.switchTo().defaultContent();
    }

    private void submit(WebElement element) {
        element.submit();
        driver.switchTo().defaultContent();
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            addToLog("INFO: Verdeckter Button umgangen");
            System.out.println("Umgehe versteckten Button:"+e.getMessage());

            js.executeScript("arguments[0].click()", element);
        }
        driver.switchTo().defaultContent();
    }

    private void highlightElement(WebElement element, String text) {
        js.executeScript("arguments[0].setAttribute('style', 'border:2px solid #c95c55;')", element);

        js.executeScript("var d = document.createElement('div');" +
                "d.setAttribute('style','position:relative;display:inline-block;');" +
                "var s = document.createElement('span');" +
                "s.setAttribute('style','background-color:#c95c55;color:white;position:absolute;bottom:125%;left:50%;padding:0 5px;');" +
                "var t = document.createTextNode('"+text+"');"+
                "s.appendChild(t);" +
                "d.appendChild(s);" +
                "arguments[0].appendChild(d);", element);
    }

    protected void quit() {
        if(driver != null) {
            addToLog("INFO: Browser wurde beendet");
            driver.quit();
            driver = null;
        }
    }

    protected void addToLog(String line) {
        logText.set(this.logText.getValue() +"\n" + line);
    }

    private WebElement recursiveSearch(IdentType type, String identifier, List<WebElement> elements, int depth) {
        // search in multiple frames
        if (depth >= IFRAME_SEARCH_DEPTH) return null;

        WebElement element;
        for(WebElement frame : elements) {
            driver.switchTo().frame(frame);

            element = extractElementsByType(type, identifier);
            if(element != null) return element;
            element = recursiveSearch(type, identifier, driver.findElements(By.tagName("iframe")), depth+1);
            if(element != null) return element;

            driver.switchTo().parentFrame();
        }
        return null;
    }

}
