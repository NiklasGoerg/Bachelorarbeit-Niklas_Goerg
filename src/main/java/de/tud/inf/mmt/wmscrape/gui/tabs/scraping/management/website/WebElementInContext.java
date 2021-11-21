package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import org.openqa.selenium.WebElement;

public class WebElementInContext {


    private final WebElement element;
    private final WebElement frame;
    private final int parentId;
    private final int id;

    public WebElementInContext(WebElement element, WebElement frame, int id ,int parentId) {
        this.element = element;
        this.parentId = parentId;
        this.frame = frame;
        this.id = id;
    }

    public WebElement get() {
        return element;
    }

    public WebElement getFrame() {
        return frame;
    }

    public int parentId() {
        return parentId;
    }

    public int getId() {
        return id;
    }
}
