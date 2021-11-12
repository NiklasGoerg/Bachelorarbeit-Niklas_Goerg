package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

@Entity
public class ElementCorrelation {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "identType")
    @Enumerated(EnumType.STRING)
    private IdentTypeDeactivated _identType = IdentTypeDeactivated.DEAKTIVIERT;
    @Transient
    private SimpleStringProperty identType = new SimpleStringProperty(_identType.name());
    @Column(name = "representation")
    private String _representation;
    @Transient
    private SimpleStringProperty representation = new SimpleStringProperty();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteElementId")
    private WebsiteElement websiteElement;

    // optional for stock correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockDataTableColumnId")
    private StockDataDbTableColumn stockDataTableColumn;

    // optional for course correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseDataTableColumnId")
    private CourseDataDbTableColumn courseDataTableColumn;

    // optional for exchange correlations
    private String exchangeFieldName;

    @Transient
    private boolean isChanged = false;

    @PostLoad
    private void setPropertiesFromPersistence() {
        identType.set(_identType.name());
        representation.set(_representation);
        initListener();
    }

    public ElementCorrelation() {}

    public ElementCorrelation(WebsiteElement websiteElement, StockDataDbTableColumn stockDataTableColumn) {
        this.websiteElement = websiteElement;
        this.stockDataTableColumn = stockDataTableColumn;
        initListener();
    }

    public ElementCorrelation(WebsiteElement websiteElement, CourseDataDbTableColumn courseDataTableColumn) {
        this.websiteElement = websiteElement;
        this.courseDataTableColumn = courseDataTableColumn;
        initListener();
    }

    public ElementCorrelation(WebsiteElement websiteElement, String exchangeFieldName) {
        this.websiteElement = websiteElement;
        this.exchangeFieldName = exchangeFieldName;
        initListener();
    }

    public String getIdentType() {
        return identType.get();
    }

    public SimpleStringProperty identTypeProperty() {
        return identType;
    }

    public void setIdentType(String identType) {
        this.identType.set(identType);
    }

    public String getRepresentation() {
        return representation.get();
    }

    public void setRepresentation(String representation) {
        this.representation.set(representation);
    }

    public SimpleStringProperty representationProperty() {
        return representation;
    }

    public WebsiteElement getWebsiteElement() {
        return websiteElement;
    }

    public StockDataDbTableColumn getStockDataTableColumn() {
        return stockDataTableColumn;
    }

    public CourseDataDbTableColumn getCourseDataTableColumn() {
        return courseDataTableColumn;
    }

    public String getExchangeFieldName() {
        return exchangeFieldName;
    }

    public boolean isChanged() {
        return isChanged;
    }

    private void initListener() {
        identType.addListener((o, ov, nv) -> {
            isChanged = true;
            _identType = IdentTypeDeactivated.valueOf(nv);
        });
        representation.addListener((o, ov, nv ) -> {
            isChanged = true;
            _representation = nv;
        });
    }
}
