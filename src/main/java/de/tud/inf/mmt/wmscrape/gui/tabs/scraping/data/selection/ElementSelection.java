package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection;

import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.historic.HistoricColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.beans.property.SimpleBooleanProperty;

import javax.persistence.*;
import java.util.Objects;

/**
 * stores the selected elements corresponding to one website element configuration
 */
@Entity
@Table(name = "webseiten_element_auswahl")
public class ElementSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(mappedBy = "elementSelection", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private ElementDescCorrelation elementDescCorrelation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "website_element_id", referencedColumnName = "id", nullable = false)
    private WebsiteElement websiteElement;

    // optional, only stock/course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_isin", referencedColumnName = "isin", updatable = false)
    private Stock stock;

    // optional, only exchange
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_column_id", referencedColumnName = "id", updatable = false)
    private ExchangeColumn exchangeColumn;

    // optional for historic correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historic_column_id", referencedColumnName = "id", updatable = false)
    private HistoricColumn historicColumn;

    @Column(name = "is_selected", nullable = false)
    private boolean _selected = false;

    @Transient
    private String description;

    @Transient
    private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    // optional, only stock/course
    // ony used to circumvent the closed proxy session
    @Transient
    private String isin;
    @Transient
    private String wkn;

    @Transient
    private boolean isChanged = false;

    // used in table extraction to ignore already extracted selection
    @Transient
    private boolean wasExtracted = false;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public ElementSelection() {}

    public ElementSelection(WebsiteElement websiteElement, Stock stock) {
        this.websiteElement = websiteElement;
        this.stock = stock;
        setPropertiesFromPersistence();
    }

    public ElementSelection(WebsiteElement websiteElement, ExchangeColumn exchangeColumn) {
        this.websiteElement = websiteElement;
        this.exchangeColumn = exchangeColumn;
        setPropertiesFromPersistence();
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

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
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

    /**
     * called after entity creation by hibernate (loading from the database)
     * updates the property values to those from the database
     */
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

    /**
     * due to the fact that hibernate creates proxies (subclasses of the actual entities) one has to use "instanceof" to compare
     * objects. normally checking of equality can cause unexpected results.
     * lazy loaded fields are omitted because one can not know if a session is still attached.
     *
     * @param o the object to compare to
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElementSelection)) return false;
        ElementSelection that;
        that = (ElementSelection) o;
        return Objects.equals(description, that.description) && Objects.equals(isin, that.isin);
    }

    /**
     * allows using properties which can't be stored by hibernate.
     * when a property changes the filed inside the entity changes which can be stored as usual
     */
    private void initListener() {
        selected.addListener((o,ov,nv) -> {
            isChanged = true;
            _selected = nv;
        });
    }

    public WebsiteElement getWebsiteElement() {
        return websiteElement;
    }
}
