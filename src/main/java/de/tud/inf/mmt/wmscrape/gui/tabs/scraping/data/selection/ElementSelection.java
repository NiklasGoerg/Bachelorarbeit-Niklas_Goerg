package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection;

import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.beans.property.SimpleBooleanProperty;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class ElementSelection {

    @Id
    @GeneratedValue
    private int id;

    @Transient
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteElementId", referencedColumnName = "id")
    private WebsiteElement websiteElement;

    @Column(name = "isSelected")
    private boolean _selected = false;
    @Transient
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    // optional, only stock/course
    // ony used to circumvent the closed proxy session
    @Transient
    private String isin;
    @Transient
    private String wkn;

    // optional, only stock/course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockIsin", referencedColumnName = "isin")
    private Stock stock;

    // optional, only exchange
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchangeDataDbTableColumnId", referencedColumnName = "id")
    private ExchangeColumn exchangeColumn;

    @OneToOne(mappedBy = "elementSelection", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private ElementDescCorrelation elementDescCorrelation;

    @Transient
    private boolean isChanged = false;

    // used in table extraction to ignore already extracted selection
    @Transient
    private boolean wasExtracted = false;


    public ElementSelection() {}

    public ElementSelection(WebsiteElement websiteElement, Stock stock) {
        //this.description = stock.getName();
        this.websiteElement = websiteElement;
        this.stock = stock;
        setPropertiesFromPersistence();
    }

    public ElementSelection(WebsiteElement websiteElement, ExchangeColumn exchangeColumn) {
        //this.description = exchangeColumn.getName();
        this.websiteElement = websiteElement;
        this.exchangeColumn = exchangeColumn;
        setPropertiesFromPersistence();
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

    public String getWkn() {
        return wkn;
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

    public ElementDescCorrelation getElementDescCorrelation() {
        return elementDescCorrelation;
    }

    public void setElementDescCorrelation(ElementDescCorrelation elementDescCorrelation) {
        this.elementDescCorrelation = elementDescCorrelation;
    }

    public boolean wasExtracted() {
        return wasExtracted;
    }

    public void isExtracted() {
        this.wasExtracted = true;
    }

    @PostLoad
    private void setPropertiesFromPersistence() {
        selected.set(_selected);
        if(stock != null) {
            isin = stock.getIsin();
            wkn = stock.getWkn();
            description = stock.getName();
        } else if(exchangeColumn != null) {
            description = exchangeColumn.getName();
        }
        initListener();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElementSelection)) return false;
        ElementSelection that;
        that = (ElementSelection) o;
        return Objects.equals(description, that.description) && Objects.equals(isin, that.isin);
    }

    private void initListener() {
        selected.addListener((o,ov,nv) -> {
            isChanged = true;
            _selected = nv;
        });
    }
}
