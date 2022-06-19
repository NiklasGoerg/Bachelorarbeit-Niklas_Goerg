package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * class which represents the rows of the stock selection table
 * in the stock parameter sub-tab
 */
public class StockSelection {
    private String wkn;
    private String isin;
    private String name;
    private SimpleBooleanProperty isSelected;

    private final SimpleBooleanProperty isParameterSelected = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isWatchListSelected = new SimpleBooleanProperty(false);

    public StockSelection(String wkn, String isin, String name, boolean isSelected) {
        this.wkn = wkn;
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

    public String getWkn() {
        return wkn;
    }

    public void setWkn(String wkn) {
        this.wkn = wkn;
    }

    public boolean isWatchListSelected() {
        return isWatchListSelected.get();
    }

    public SimpleBooleanProperty isWatchListSelectedProperty() {
        return isWatchListSelected;
    }

    public boolean isTransactionSelected() {
        return isParameterSelected.get();
    }

    public SimpleBooleanProperty isTransactionSelectedProperty() {
        return isParameterSelected;
    }
}
