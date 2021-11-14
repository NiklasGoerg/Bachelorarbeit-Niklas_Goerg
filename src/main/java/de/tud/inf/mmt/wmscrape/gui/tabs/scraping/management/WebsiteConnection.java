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

        WebElement element = findElementByType(type.ordinal(), website.getCookieAcceptIdent());

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

        WebElement username = findElementByType(website.getUsernameIdentType().ordinal(), website.getUsernameIdent());
        if(username == null) return false;
        username.sendKeys(website.getUsername());

        WebElement password = findElementByType(website.getPasswordIdentType().ordinal(), website.getPasswordIdent());
        if(password == null) return false;
        password.sendKeys(website.getPassword());

        addToLog("INFO: Login Informationen ausgefüllt");
        return true;
    }

    protected boolean login() {
        WebElement password = findElementByType(website.getPasswordIdentType().ordinal(), website.getPasswordIdent());

        // submit like pressing enter
        if(website.getLoginButtonIdentType() == IdentType.DEAKTIVIERT) {
            password.submit();
            return true;
        }

        WebElement loginButton = findElementByType(website.getLoginButtonIdentType().ordinal(), website.getLoginButtonIdent());
        if(loginButton == null) return false;
        clickElement(loginButton);

        addToLog("INFO: Login erfolgreich");
        return true;
    }

    protected boolean logout() {
        IdentType type = website.getLogoutIdentType();
        if(type == IdentType.DEAKTIVIERT) return true;

        if(website.getCookieHideIdentType() != IdentType.DEAKTIVIERT) hideCookies();

        if(type == IdentType.URL) {
            driver.get(website.getLogoutIdent());
            return true;
        }

        WebElement logoutButton = findElementByType(type.ordinal(), website.getLogoutIdent());
        if(logoutButton == null) return false;
        clickElement(logoutButton);

        addToLog("INFO: Logout erfolgreich");
        return true;
    }

    public void loadPage(String url) {
        driver.get(url);
        waitLoadEvent();
        addToLog("INFO: "+website.getUrl()+" geladen");
    }

    private void waitLoadEvent() {
        wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
    }

    private WebElement findElementByType(int type, String identifier) {
        // called separately in tester
        if(website.getCookieHideIdentType() != IdentType.DEAKTIVIERT
                && !(this instanceof WebsiteTester)) hideCookies();

        // little trick: my ident enums always start with id, xpath, css
        // therefore id=0, xpath=1, css=2

        List<WebElement> elements;

        if(type == 0) {
            elements = driver.findElements(By.id(identifier));
        } else if(type == 1) {
            elements = driver.findElements(By.xpath(identifier));
        } else if(type==2) {
            elements = driver.findElements(By.cssSelector(identifier));
        } else return null;

        // found element in main document
        if(elements.size()>0) return elements.get(0);

        // search in multiple frames
        for(WebElement frame : driver.findElements(By.tagName("iframe"))) {
            driver.switchTo().frame(frame);

            if(type == 0) {
                elements = driver.findElements(By.id(identifier));
            } else if(type == 1) {
                elements = driver.findElements(By.xpath(identifier));
            } else {
                elements = driver.findElements(By.cssSelector(identifier));
            }

            for (WebElement element : elements) return element;

            driver.switchTo().parentFrame();
        }

        addToLog("FEHLER: Kein Element unter "+identifier+" gefunden");
        return null;
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
