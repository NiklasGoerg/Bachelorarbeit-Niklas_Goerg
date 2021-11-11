package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection;

import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.beans.property.SimpleBooleanProperty;

import javax.persistence.*;

@Entity
public class ElementSelection {

    @Id
    @GeneratedValue
    private int id;
    private String description;
    // optional, only stock
    @Transient
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

    @Transient
    private boolean isChanged = false;

    @PostLoad
    private void setPropertiesFromPersistence() {
        selected.set(_selected);
        if(stock != null) isin = stock.getIsin();
        initListener();
    }

    public ElementSelection() {}

    public ElementSelection(String description, WebsiteElement websiteElement, Stock stock) {
        this.description = description;
        this.websiteElement = websiteElement;
        this.stock = stock;
        this.isin = stock.getIsin();
        initListener();
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

    public boolean isChanged() {
        return isChanged;
    }

    private void initListener() {
        selected.addListener((o,ov,nv) -> {
            isChanged = true;
            _selected = nv;
        });
    }
}
