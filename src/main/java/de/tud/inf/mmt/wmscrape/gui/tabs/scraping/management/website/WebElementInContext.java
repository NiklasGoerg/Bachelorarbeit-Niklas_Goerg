package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public record WebElementInContext(WebElement element, WebElement frame,
                                  SearchContext context) {

    public WebElement get() {
        return element;
    }

    // not used atm
    // represents the parent
    public SearchContext getContext() {
        return context;
    }

    public WebElement getFrame() {
        return frame;
    }
}
