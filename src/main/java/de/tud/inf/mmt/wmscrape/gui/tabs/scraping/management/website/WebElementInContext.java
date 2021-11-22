package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import org.openqa.selenium.WebElement;

public record WebElementInContext(WebElement element, WebElement frame, int id,
                                  int parentId) {

    public WebElement get() {
        return element;
    }

    public WebElement getFrame() {
        return frame;
    }

    public int getId() {
        return id;
    }
}
