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
import java.util.ArrayList;
import java.util.List;

public abstract class WebsiteHandler {

    public static final int IFRAME_SEARCH_DEPTH = 3;

    protected final Website website;
    protected final boolean headless;
    protected WebDriver driver;
    protected JavascriptExecutor js;

    private int waitForWsElementSec = 5;
    private WebDriverWait wait;
    private final SimpleStringProperty logText;

    public WebsiteHandler(Website website) {
        this.website = website;
        headless = true;
        this.logText = new SimpleStringProperty();
    }

    public WebsiteHandler(Website website, SimpleStringProperty logText, Boolean headless) {
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

        if (headless) options.setHeadless(true);

        driver = new FirefoxDriver(options);
        js = (JavascriptExecutor) driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(waitForWsElementSec));
    }

    protected void loadLoginPage() {
        loadPage(website.getUrl());
    }

    protected boolean acceptCookies() {
        IdentType type = website.getCookieAcceptIdentType();
        if (type == IdentType.DEAKTIVIERT) return true;

        WebElement element = extractElementFromRoot(type, website.getCookieAcceptIdent());

        if (element == null) return false;

        clickElement(element);
        addToLog("INFO: Cookies akzeptiert");
        return true;
    }

    protected boolean hideCookies() {
        IdentType type = website.getCookieHideIdentType();
        if (type == IdentType.DEAKTIVIERT) return true;

        String identifier = website.getCookieHideIdent();

        WebElement banner = extractElementFromRoot(type, identifier);
        if (banner != null) {
            js.executeScript("(arguments[0]).remove()", banner);
        } else {
            addToLog("WARNUNG: Cookiebanner nicht gefunden");
            return false;
        }

        addToLog("INFO: Cookiebanner ausgeblendet");
        return true;
    }

    protected boolean fillLoginInformation() {

        WebElement username = extractElementFromRoot(website.getUsernameIdentType(), website.getUsernameIdent());
        if (username == null) return false;
        setText(username, website.getUsername());

        WebElement password = extractElementFromRoot(website.getPasswordIdentType(), website.getPasswordIdent());
        if (password == null) return false;
        setText(password, website.getPassword());

        addToLog("INFO: Login Informationen ausgefüllt");
        return true;
    }

    protected boolean login() {
        if (website.getLoginButtonIdentType() == IdentType.ENTER) {
            WebElement password = extractElementFromRoot(website.getPasswordIdentType(), website.getPasswordIdent());
            if(password != null) {
                // submit like pressing enter
                submit(password);
                return true;
            }
            return false;
        }

        WebElement loginButton = extractElementFromRoot(website.getLoginButtonIdentType(), website.getLoginButtonIdent());
        if (loginButton == null) return false;
        clickElement(loginButton);

        addToLog("INFO: Login erfolgreich");
        return true;
    }

    protected boolean logout() {
        IdentType type = website.getLogoutIdentType();
        if (type == IdentType.DEAKTIVIERT) return true;

        // called separately in tester
        if (website.getCookieHideIdentType() != IdentType.DEAKTIVIERT
                && !(this instanceof WebsiteTester)) hideCookies();

        if (type == IdentType.URL) {
            driver.get(website.getLogoutIdent());
            addToLog("INFO: Logout erfolgreich");
            return true;
        }

        WebElement logoutButton = extractElementFromRoot(type, website.getLogoutIdent());
        if (logoutButton == null) return false;
        clickElement(logoutButton);

        addToLog("INFO: Logout erfolgreich");
        return true;
    }

    protected void loadPage(String url) {
        driver.get(url);
        waitLoadEvent();

        // called separately in tester
        if (website.getCookieHideIdentType() != IdentType.DEAKTIVIERT
                && !(this instanceof WebsiteTester)) hideCookies();

        addToLog("INFO: " + url + " geladen");
    }

    private void waitLoadEvent() {
        wait.until(webDriver -> js.executeScript("return document.readyState").equals("complete"));
    }

    public WebElement extractElementFromRoot(IdentType type, String identifier) {
        // solely the selenium element without iframe or relation context
        var elements = extractAllFramesFromContext(type, identifier, null);
        if (elements != null && elements.size() > 0) return elements.get(0).get();
        return null;
    }

    public WebElement extractElementFromContext(IdentType type, String identifier, WebElementInContext webElementInContext) {
        var elements = extractAllFramesFromContext(type, identifier, webElementInContext);
        if (elements != null) return elements.get(0).get();
        return null;
    }

    public WebElementInContext extractFrameElementFromRoot(IdentType type, String identifier) {
        // a frame element containing the selenium element and iframe/context information
        var elements = extractAllFramesFromContext(type, identifier, null);
        if (elements != null && elements.size() > 0) return elements.get(0);
        return null;
    }

    private void switchToFrame(WebElement frame) {
        if(frame != null) driver.switchTo().frame(frame);
        else driver.switchTo().defaultContent();
    }

    /**
     * @param webElementInContext null uses the driver context
     */
    public List<WebElementInContext> extractAllFramesFromContext(IdentType type, String identifier, WebElementInContext webElementInContext) {

        SearchContext context = driver;
        WebElement frame = null;

        if(webElementInContext != null) {
            context = webElementInContext.getContext();
            frame = webElementInContext.getFrame();
            switchToFrame(webElementInContext.getFrame());
        } else {
            switchToFrame(null);
        }

        // search elements in root
        var webElements = findElementsRelative(context, type, identifier);

        if (webElements != null && webElements.size() > 0) return webToFrameList(webElements, frame, context);

        // search in sub iframes recursively
        List<WebElementInContext> webElementInContexts = recursiveSearch(context, type, identifier, context.findElements(By.tagName("iframe")), 0);
        if (webElementInContexts != null) return webElementInContexts;

        addToLog("FEHLER: Keine Elemente unter '" + identifier + "' gefunden");
        return null;
    }

    private List<WebElementInContext> webToFrameList(List<WebElement> webElements, WebElement frame, SearchContext context) {
        List<WebElementInContext> webElementInContexts = new ArrayList<>();
        for (WebElement element : webElements) {
            webElementInContexts.add(new WebElementInContext(element, frame, context));
        }
        return webElementInContexts;
    }

    private List<WebElement> findElementsRelative(SearchContext context, IdentType type, String identifier) {
        List<WebElement> elements;
        try {
            switch (type) {
                case ID -> elements = context.findElements(By.id(identifier));
                case XPATH -> elements = context.findElements(By.xpath(identifier));
                case CSS -> elements = context.findElements(By.cssSelector(identifier));
                case TAG -> elements = context.findElements(By.tagName(identifier));
                default -> elements = null;
            }
        } catch (InvalidSelectorException e) {
            addToLog("FEHLER: Invalider Identifizierer '"+identifier+"'");
            return null;
        }
        return elements;
    }

    private List<WebElementInContext> recursiveSearch(SearchContext context, IdentType type, String identifier,
                                                      List<WebElement> iframes, int depth) {
        // nothing found in max depth. return
        if (depth >= IFRAME_SEARCH_DEPTH) return null;

        List<WebElementInContext> webElementInContexts;

        // search every iframe
        for (WebElement frame : iframes) {
            driver.switchTo().frame(frame);

            // look inside the frame
            var webElements = findElementsRelative(context, type, identifier);

            // found something
            if(webElements != null && webElements.size() > 0) {
                return webToFrameList(webElements, frame, context);
            }

            // found nothing, search again in the sub-frames inside this frame
            webElementInContexts = recursiveSearch(context, type, identifier, driver.findElements(By.tagName("iframe")), depth + 1);
            if (webElementInContexts != null) return webElementInContexts;

            driver.switchTo().parentFrame();
        }
        return null;
    }

    // only call in respective frame
    private void setText(WebElement element, String text) {
        element.sendKeys(text);
    }

    // only call in respective frame
    private void submit(WebElement element) {
        element.submit();
    }

    // only call in respective frame
    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            addToLog("INFO: Verdeckter Button erkannt");
            js.executeScript("arguments[0].click()", element);
        }
    }

    protected void quit() {
        if (driver != null) {
            addToLog("INFO: Browser wurde beendet");
            driver.quit();
            driver = null;
        }
    }

    protected void addToLog(String line) {
        logText.set(this.logText.getValue() + "\n" + line);
    }

}
