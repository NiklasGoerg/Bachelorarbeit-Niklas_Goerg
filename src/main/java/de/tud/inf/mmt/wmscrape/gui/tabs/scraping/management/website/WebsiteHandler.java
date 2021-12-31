package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class WebsiteHandler extends Service<Void> {

    public static final int IFRAME_SEARCH_DEPTH = 3;

    protected final boolean headless;
    private final SimpleStringProperty logText;

    protected Website website;
    protected FirefoxDriver driver;
    private long waitForWsElementSec = 3;
    private WebDriverWait wait;

    private int uniqueElementId = 0;

    protected WebsiteHandler(SimpleStringProperty logText, Boolean headless) {
        this.headless = headless;
        this.logText = logText;
    }

    public WebsiteHandler(Website website, SimpleStringProperty logText, Boolean headless) {
        this(logText, headless);
        this.website = website;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setWaitForWsElementSec(double waitForWsElementSec) {
        this.waitForWsElementSec = (long) waitForWsElementSec;
    }

    public void waitForWsElements(boolean doWait) {
        if(driver == null) return;

        Duration d;
        if(doWait) { d = Duration.ofMillis((waitForWsElementSec*1000)); }
        else { d = Duration.ofMillis(0); }

        wait = new WebDriverWait(driver, d);
        driver.manage().timeouts().implicitlyWait(d);
    }

    protected WebDriver getDriver() {
        return driver;
    }

    protected boolean startBrowser() {
        try {
            if(driver != null && browserIsOpen()) {
                addToLog("INFO:\tBrowser noch geöffnet. Setze fort.");
                return true;
            }

            FirefoxBinary firefoxBinary = new FirefoxBinary();
            FirefoxOptions options = new FirefoxOptions();
            options.setBinary(firefoxBinary);
            options.setLogLevel(FirefoxDriverLogLevel.FATAL);

            if (headless) options.setHeadless(true);

            driver = new FirefoxDriver(options);
            waitForWsElements(true);

            return true;
        } catch (SessionNotCreatedException e) {
            addToLog("ERR:\t\t Selenium konnte nicht gestartet werden.\n\n"+e.getMessage()+"\n\n"+e.getCause()+"\n\n");
            return false;
        }
    }

    // thanks selenium for not providing information
    protected boolean browserIsOpen() {
        try{
            driver.getTitle();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    protected boolean loadLoginPage() {
        return loadPage(website.getUrl());
    }

    protected boolean acceptCookies() {
        IdentType type = website.getCookieAcceptIdentType();
        if (type == IdentType.DEAKTIVIERT) return true;

        WebElement element = extractElementFromRoot(type, website.getCookieAcceptIdent());

        if (element == null) return false;

        clickElement(element);
        addToLog("INFO:\tCookies akzeptiert");
        return true;
    }

    protected boolean fillLoginInformation() {

        WebElement username = extractElementFromRoot(website.getUsernameIdentType(), website.getUsernameIdent());
        if (username == null) return false;
        setText(username, website.getUsername());

        WebElement password = extractElementFromRoot(website.getPasswordIdentType(), website.getPasswordIdent());
        if (password == null) return false;
        setText(password, website.getPassword());

        addToLog("INFO:\tLogin Informationen ausgefüllt");
        return true;
    }

    protected boolean login() {
        if (website.getLoginButtonIdentType() == IdentType.ENTER) {
            WebElement password = extractElementFromRoot(website.getPasswordIdentType(), website.getPasswordIdent());
            if(password != null) {
                // submit like pressing enter
                submit(password);
                addToLog("INFO:\tLogin erfolgreich");
                return true;
            }
            return false;
        }

        WebElement loginButton = extractElementFromRoot(website.getLoginButtonIdentType(), website.getLoginButtonIdent());
        if (loginButton == null) return false;
        clickElement(loginButton);
        addToLog("INFO:\tLogin erfolgreich");
        return true;
    }

    protected boolean logout() {
        if(website == null) return false;

        IdentType type = website.getLogoutIdentType();
        if (type == IdentType.DEAKTIVIERT) return true;

        if (type == IdentType.URL) {
            driver.get(website.getLogoutIdent());
            waitLoadEvent();
            addToLog("INFO:\tLogout erfolgreich");
            return true;
        }

        WebElement logoutButton = extractElementFromRoot(type, website.getLogoutIdent());
        if (logoutButton == null) {
            addToLog("ERR:\t\tLogout fehlgeschlagen");
            return false;
        }

        clickElement(logoutButton);
        waitLoadEvent();

        addToLog("INFO:\tLogout erfolgreich");
        return true;
    }

    protected void waitLoadEvent() {
        try {
            // sleep otherwise it possibly checks the current side which is ready bcs. it takes some time to respond
            Thread.sleep(1000);
            wait.until(webDriver -> driver.executeScript("return document.readyState").equals("complete"));
        } catch (Exception e) {
            System.out.println(e.getMessage()+" "+ e.getCause());
        }
    }

    protected boolean loadPage(String url) {
        // reset ids for a new page
        uniqueElementId = 0;

        try {
            driver.get(url);
        } catch (WebDriverException e) {
            System.out.println(e.getMessage()+" <-> "+e.getCause());
            return false;
        }

        addToLog("INFO:\t" + url + " geladen");
        return true;
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

    public WebElementInContext extractFrameElementFromContext(IdentType type, String identifier, WebElementInContext context) {
        // a frame element containing the selenium element and iframe/context information
        var elements = extractAllFramesFromContext(type, identifier,  context);
        if (elements != null && elements.size() > 0) return elements.get(0);
        return null;
    }

    private void switchToFrame(WebElement frame) {
        if(frame != null) driver.switchTo().frame(frame);
        else driver.switchTo().defaultContent();
    }

    /**
     * @param parent null uses the driver context
     */
    public List<WebElementInContext> extractAllFramesFromContext(IdentType type, String ident, WebElementInContext parent) {

        int parentId = 0;
        WebElement frame = null;
        SearchContext searchContext = driver;
        String identifier = ident;

        // switches to the parents frame and changes the xpath if used
        if(parent != null) {
            frame = parent.getFrame();
            searchContext = parent.get();
            parentId = parent.getId();
            identifier = enhancedIdentifier(type, identifier, parentId);
        }

        switchToFrame(frame);

        try {
            // search elements in root
            var webElements = findElementsRelative(searchContext, type, identifier);
            if (webElements != null && webElements.size() > 0) return toContextList(webElements, frame, parentId);


            // search in sub iframes recursively
            List<WebElementInContext> webElementInContexts = recursiveSearch(searchContext, type, identifier,
                    searchContext.findElements(By.tagName("iframe")), 0, parentId);


            if (webElementInContexts != null) return webElementInContexts;
            addToLog("ERR:\t\tKeine Elemente unter '" + ident + "' gefunden");

        } catch (InvalidSelectorException e) {
            e.printStackTrace();
            addToLog("ERR:\t\tInvalider Identifizierer vom Typ "+type+": '"+ident+"'");
        }

        return null;
    }

    private List<WebElementInContext> toContextList(List<WebElement> webElements, WebElement frame, int parentId) {
        List<WebElementInContext> webElementInContexts = new ArrayList<>();

        for (var element : webElements) {
            uniqueElementId++;
            webElementInContexts.add(new WebElementInContext(element, frame, uniqueElementId, parentId));
            setUniqueId(element, uniqueElementId); // sets the id inside the html code

        }
        return webElementInContexts;
    }

    private List<WebElement> findElementsRelative(SearchContext context, IdentType type, String identifier) throws InvalidSelectorException {
        List<WebElement> elements;
        switch (type) {
            case ID -> elements = context.findElements(By.id(identifier));
            case XPATH -> elements = context.findElements(By.xpath(identifier));
            case CSS -> elements = context.findElements(By.cssSelector(identifier));
            case TAG -> elements = context.findElements(By.tagName(identifier));
            default -> elements = null;
        }
        return elements;
    }

    private void setUniqueId(WebElement element, int id) {
        if(element == null) return;
        try {
            driver.executeScript("arguments[0].setAttribute('wms', "+id+")", element);
        } catch (Exception e) {
            System.out.println(e.getMessage()+" "+ e.getCause());
        }

    }

    // creates an absolute xpath based on the id priorly set
    private String enhancedIdentifier(IdentType type, String identifier, int parenId) {
        if(type == IdentType.XPATH) {
            checkRelativeXPath(identifier);
            return "//*[@wms=" + parenId + "]" + identifier;
        }
        return identifier;
    }

    private void checkRelativeXPath(String identifier) {
        if(!identifier.startsWith("/")) {
            addToLog("WARN:\tPotentiell fehlgeformter XPath '"+identifier+"'. Ein Unterpfad sollt mit / oder // beginnen.");
        }
    }


    private List<WebElementInContext> recursiveSearch(SearchContext context, IdentType type, String identifier,
                                                      List<WebElement> iframes, int depth, int parentId) throws InvalidSelectorException {
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
                return toContextList(webElements, frame, parentId);
            }

            // found nothing, search again in the sub-frames inside this frame
            webElementInContexts = recursiveSearch(context, type, identifier, driver.findElements(By.tagName("iframe")), depth + 1, parentId);
            if (webElementInContexts != null) return webElementInContexts;

            driver.switchTo().parentFrame();
        }
        return null;
    }

    // only call in respective frame
    private void setText(WebElement element, String text) {
        if(element == null) return;
        element.sendKeys(text);
    }

    // only call in respective frame
    private void submit(WebElement element) {
        if(element == null) return;
        element.submit();
    }

    // only call in respective frame
    private void clickElement(WebElement element) {
        if(element == null) return;

        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            addToLog("INFO:\tVerdeckter Button erkannt");
            driver.executeScript("arguments[0].click()", element);
        } catch (Exception e) {
            System.out.println(e.getMessage()+" "+ e.getCause());
        }
    }

    protected void quit() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
                addToLog("INFO:\tBrowser wurde beendet");
            }
        } catch (Exception e) {
            addToLog("ERR:\t\tFehler beim Schließen des Browsers.\n\n"+e.getMessage()+"\n\n"+e.getCause()+"\n\n");
        }
    }

    protected void addToLog(String line) {
        // not doing this would we be a problem due to the multithreaded execution
        Platform.runLater(() -> logText.set(this.logText.getValue() + "\n" + line));
    }

}
