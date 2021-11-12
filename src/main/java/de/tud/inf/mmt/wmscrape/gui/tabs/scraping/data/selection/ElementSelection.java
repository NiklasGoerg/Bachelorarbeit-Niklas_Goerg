package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection;

import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.beans.property.SimpleBooleanProperty;

import javax.persistence.*;
import java.util.Objects;

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

    // optional, only stock/course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockIsin")
    private Stock stock;

    // optional, only exchange
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchangeDataDbTableColumnId")
    private ExchangeDataDbTableColumn exchangeDataDbTableColumn;

    @OneToOne(mappedBy = "elementSelection", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private ElementDescCorrelation elementDescCorrelation;

    @Transient
    private boolean isChanged = false;

    @PostLoad
    private void setPropertiesFromPersistence() {
        selected.set(_selected);
        if(stock != null) isin = stock.getIsin();
        initListener();
    }

    public ElementSelection() {}

    public ElementSelection(WebsiteElement websiteElement, Stock stock) {
        this.description = stock.getName();
        this.websiteElement = websiteElement;
        this.stock = stock;
        this.isin = stock.getIsin();
        initListener();
    }

    public ElementSelection(WebsiteElement websiteElement, ExchangeDataDbTableColumn exchangeDataDbTableColumn) {
        this.description = exchangeDataDbTableColumn.getName();
        this.websiteElement = websiteElement;
        this.exchangeDataDbTableColumn = exchangeDataDbTableColumn;
        initListener();
    }

    public int getId() {
        return id;
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

    public ExchangeDataDbTableColumn getExchangeDataDbTableColumn() {
        return exchangeDataDbTableColumn;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public ElementDescCorrelation getElementDescCorrelation() {
        return elementDescCorrelation;
    }

    public void setElementDescCorrelation(ElementDescCorrelation elementDescCorrelation) {
        this.elementDescCorrelation = elementDescCorrelation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementSelection that = (ElementSelection) o;
        return Objects.equals(description, that.description) && Objects.equals(isin, that.isin);
    }

    private void initListener() {
        selected.addListener((o,ov,nv) -> {
            isChanged = true;
            _selected = nv;
        });
    }
}
