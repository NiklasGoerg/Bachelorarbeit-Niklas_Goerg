package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.Stock;
import javafx.beans.property.SimpleBooleanProperty;

import javax.persistence.*;

@Entity
public class ElementSelection {

    @Id
    @GeneratedValue
    private int id;
    private String description;
    // optional, only stock
    private String isin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteElementId")
    private WebsiteElement websiteElement;

    @Column(name = "isSelected")
    private boolean _selected = false;
    @Transient
    private SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    // optional, only stock
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockIsin")
    private Stock stock;

    @PostLoad
    private void setPropertiesFromPersistence() {
        selected.set(_selected);
    }

    @PrePersist
    private void updateFromProperty() {
        _selected = selected.get();
    }

    public ElementSelection() {}

    public ElementSelection(String description, String isin, WebsiteElement websiteElement, Stock stock) {
        this.description = description;
        this.isin = isin;
        this.websiteElement = websiteElement;
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public String getIsin() {
        return isin;
    }

    public WebsiteElement getWebsiteElement() {
        return websiteElement;
    }

    public void setWebsiteElement(WebsiteElement websiteElement) {
        this.websiteElement = websiteElement;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public Stock getStock() {
        return stock;
    }


    // only valuable to persist only those who are selected for the first time
    // no selection _selected==selected.get()=false -> useless
    // selection _selected!=selected.get() false=!true -> save
    public boolean isChanged() {
        return _selected != selected.get();
    }
}
