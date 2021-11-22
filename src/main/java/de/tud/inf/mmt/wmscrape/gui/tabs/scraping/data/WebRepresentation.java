package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import java.util.List;

// only used to represent different objects in a tree view
public abstract class WebRepresentation<T extends WebRepresentation<?>> {

    public WebRepresentation() {}

    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract List<T> getChildren();
}
