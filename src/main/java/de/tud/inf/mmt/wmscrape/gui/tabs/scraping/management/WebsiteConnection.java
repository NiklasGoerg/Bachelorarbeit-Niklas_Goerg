package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

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

        // delete the frame completely
        if(type == IdentType.ID) {
            js.executeScript("const frames = document.getElementsByTagName(\"iframe\");" +
                    "for (let frame of frames) { if(frame.getAttribute(\"id\") == \"" +
                    identifier +
                    "\") {frame.remove();}}");
        }

        String selectBy;
        if(type == IdentType.ID) {
            selectBy = "getElementById(\""+identifier+"\")";
        } else if (type == IdentType.XPATH) {
            selectBy = "evaluate(\""+identifier+"\", document, null, " +
                    "XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue";
        } else if (type == IdentType.CSS) {
            selectBy = "querySelector(\""+identifier+"\")";
        } else return false;


        js.executeScript("var e=document. "+selectBy+"; if(e!=null) e.remove();");
        addToLog("INFO: Cookiebanner ausgeblendet");
        return true;
    }

    protected boolean fillLoginInformation() {

        WebElement username = findElementByType(website.getUsernameIdentType(), website.getUsernameIdent());
        if(username == null) return false;
        username.sendKeys(website.getUsername());

        WebElement password = findElementByType(website.getPasswordIdentType(), website.getPasswordIdent());
        if(password == null) return false;
        password.sendKeys(website.getPassword());

        addToLog("INFO: Login Informationen ausgefüllt");
        return true;
    }

    protected boolean login() {
        WebElement password = findElementByType(website.getPasswordIdentType(), website.getPasswordIdent());

        // submit like pressing enter
        if(website.getLoginButtonIdentType() == IdentType.ENTER) {
            password.submit();
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

        // search in multiple frames
        for(WebElement frame : driver.findElements(By.tagName("iframe"))) {
            driver.switchTo().frame(frame);

            element = extractElementsByType(type, identifier);
            if(element != null) return element;

            driver.switchTo().parentFrame();
        }

        addToLog("FEHLER: Kein Element unter '"+identifier+"' gefunden");
        return null;
    }

    private WebElement extractElementsByType(IdentType type, String identifier) {
        List<WebElement> elements;

        if(type == IdentType.ID) {
            elements = driver.findElements(By.id(identifier));
        } else if(type == IdentType.XPATH) {
            elements = driver.findElements(By.xpath(identifier));
        } else if(type== IdentType.CSS) {
            elements = driver.findElements(By.cssSelector(identifier));
        } else return null;

        if(elements.size()>0) return elements.get(0);
        return null;
    }

    protected String extractTextDataByType(IdentType type, String identifier, String highlightText) {
        WebElement element = extractElementsByType(type, identifier);
        if(element == null) return null;
        String text = element.getText();
        if(!headless) highlightElement(element, highlightText);
        return text;
    }

    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            addToLog("INFO: Verdeckter Button umgangen");
            System.out.println("Umgehe versteckten Button:"+e.getMessage());

            js.executeScript("arguments[0].click()", element);
        }
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

    private void addToLog(String line) {
        logText.set(this.logText.getValue() +"\n" + line);
    }

}
