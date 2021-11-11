package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivatedUrl;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import javafx.beans.property.SimpleStringProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WebsiteManager {

    private Website website;
    private int waitDurationSec;
    private FirefoxOptions options;
    private boolean headless;
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private SimpleStringProperty logText;

    public WebsiteManager(Website website) {
        this.website = website;
        headless = true;
        waitDurationSec = 5;
    }

    public WebsiteManager(Website website, SimpleStringProperty logText) {
        this.website = website;
        headless = true;
        waitDurationSec = 5;
        this.logText = logText;
    }

    public WebsiteManager(Website website, boolean headless, int waitDurationSec, SimpleStringProperty logText) {
        this.website = website;
        this.headless = headless;
        this.waitDurationSec = waitDurationSec;
        this.logText = logText;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void startBrowser() {
        FirefoxBinary firefoxBinary = new FirefoxBinary();
        options = new FirefoxOptions();
        options.setBinary(firefoxBinary);
        options.setLogLevel(FirefoxDriverLogLevel.ERROR);

        if(headless) options.setHeadless(true);

        driver = new FirefoxDriver(options);
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(waitDurationSec));
    }

    public void loadPage() {
        driver.manage().window().maximize();
        driver.get(website.getUrl());
        waitLoadEvent();
        addToLog("INFO: "+website.getUrl()+" geladen");
    }

    public boolean acceptCookies() {
        IdentTypeDeactivated type = website.getCookieAcceptIdentType();
        if(type == IdentTypeDeactivated.DEAKTIVIERT) return true;

        WebElement element = findElementByType(type.ordinal(), website.getCookieAcceptIdent());

        if(element == null) return false;

        clickElement(element);
        addToLog("INFO: Cookies akzeptiert");
        return true;
    }

    public boolean hideCookies() {
        IdentTypeDeactivated type = website.getCookieHideIdentType();
        if(type == IdentTypeDeactivated.DEAKTIVIERT) return true;

        String identifier = website.getCookieHideIdent().replace("\"","'");

        // delete the frame completely
        if(type == IdentTypeDeactivated.ID) {
            js.executeScript("const frames = document.getElementsByTagName(\"iframe\");" +
                    "for (let frame of frames) { if(frame.getAttribute(\"id\") == \"" +
                    identifier +
                    "\") {frame.remove();}}");
        }

        String selectBy;
        if(type == IdentTypeDeactivated.ID) {
            selectBy = "getElementById(\""+identifier+"\")";
        } else if (type == IdentTypeDeactivated.XPATH) {
            selectBy = "evaluate(\""+identifier+"\", document, null, " +
                    "XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue";
        } else if (type == IdentTypeDeactivated.CSS) {
            selectBy = "querySelector(\""+identifier+"\")";
        } else return false;


        js.executeScript("var e=document. "+selectBy+"; if(e!=null) e.remove();");
        addToLog("INFO: Cookiebanner ausgeblendet");
        return true;
    }

    public boolean fillLoginInformation() {

        WebElement username = findElementByType(website.getUsernameIdentType().ordinal(), website.getUsernameIdent());
        if(username == null) return false;
        username.sendKeys(website.getUsername());

        WebElement password = findElementByType(website.getPasswordIdentType().ordinal(), website.getPasswordIdent());
        if(password == null) return false;
        password.sendKeys(website.getPassword());

        addToLog("INFO: Login Informationen ausgefüllt");
        return true;
    }

    public boolean login() {
        WebElement password = findElementByType(website.getPasswordIdentType().ordinal(), website.getPasswordIdent());

        // submit like pressing enter
        if(website.getLoginButtonIdentType() == IdentTypeDeactivated.DEAKTIVIERT) {
            password.submit();
            return true;
        }

        WebElement loginButton = findElementByType(website.getLoginButtonIdentType().ordinal(), website.getLoginButtonIdent());
        if(loginButton == null) return false;
        clickElement(loginButton);

        addToLog("INFO: Login erfolgreich");
        return true;
    }

    public boolean logout() {
        IdentTypeDeactivatedUrl type = website.getLogoutIdentType();
        if(type == IdentTypeDeactivatedUrl.DEAKTIVIERT) return true;

        if(website.getCookieHideIdentType() != IdentTypeDeactivated.DEAKTIVIERT) hideCookies();

        if(type == IdentTypeDeactivatedUrl.URL) {
            driver.get(website.getLogoutIdent());
            return true;
        }

        WebElement logoutButton = findElementByType(type.ordinal(), website.getLogoutIdent());
        if(logoutButton == null) return false;
        clickElement(logoutButton);

        addToLog("INFO: Logout erfolgreich");
        return true;
    }

    private WebElement findElementByType(int type, String identifier) {
        if(website.getCookieHideIdentType() != IdentTypeDeactivated.DEAKTIVIERT) hideCookies();

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

    private void waitLoadEvent() {
        wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
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

    public void quit() {
        if(driver != null) {
            addToLog("INFO: Browser wurde beendet");
            driver.quit();
            driver = null;
        }
    }

    public void addToLog(String line) {
        logText.set(this.logText.getValue() +"\n" + line);
    }
}
