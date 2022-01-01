package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import org.openqa.selenium.WebElement;

/**
 * contains the selenium {@link WebElement} and if existing the iframe as a {@link WebElement} where the element
 * lies in. the id and parent id are those set by scraper with javascript can be used as an absolute reference point
 * for xpath.
 */
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
