package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;

public interface Extraction {
    void extract(WebsiteElement element, Task<Void> task, SimpleDoubleProperty progress);
}
