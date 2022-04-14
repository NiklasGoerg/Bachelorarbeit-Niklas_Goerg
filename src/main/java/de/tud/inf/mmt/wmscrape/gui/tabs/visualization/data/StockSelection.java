package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data;

import javafx.beans.property.SimpleBooleanProperty;

public class StockSelection {
    private String isin;
    private String name;
    private SimpleBooleanProperty isSelected;

    public StockSelection(String isin, String name, boolean isSelected) {
        this.isin = isin;
        this.name = name;
        this.isSelected = new SimpleBooleanProperty(isSelected);
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public SimpleBooleanProperty isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = new SimpleBooleanProperty(selected);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
