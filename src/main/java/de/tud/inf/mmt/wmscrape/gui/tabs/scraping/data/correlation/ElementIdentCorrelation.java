package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

@Entity
public class ElementIdentCorrelation {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "identType")
    @Enumerated(EnumType.STRING)
    protected IdentType _identType = IdentType.DEAKTIVIERT;
    @Transient
    private final SimpleStringProperty identType = new SimpleStringProperty(_identType.name());
    @Column(name = "representation")
    private String _representation;
    @Transient
    private final SimpleStringProperty representation = new SimpleStringProperty();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteElementId", referencedColumnName = "id")
    private WebsiteElement websiteElement;

    // optional for stock correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockDataTableColumnId", referencedColumnName = "id")
    private StockDataDbTableColumn stockDataTableColumn;

    // optional for course correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseDataDbTableColumnId", referencedColumnName = "id")
    private CourseDataDbTableColumn courseDataDbTableColumn;

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

    public ElementIdentCorrelation() {}

    public ElementIdentCorrelation(WebsiteElement websiteElement, StockDataDbTableColumn stockDataTableColumn) {
        this.websiteElement = websiteElement;
        this.stockDataTableColumn = stockDataTableColumn;
        initListener();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, CourseDataDbTableColumn courseDataDbTableColumn) {
        this.websiteElement = websiteElement;
        this.courseDataDbTableColumn = courseDataDbTableColumn;
        initListener();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, String exchangeFieldName) {
        this.websiteElement = websiteElement;
        this.exchangeFieldName = exchangeFieldName;
        initListener();
    }

    public String getIdentTypeName() {
        return identType.get();
    }
    public IdentType getIdentType() {
        return IdentType.valueOf(identType.get());
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

    public CourseDataDbTableColumn getCourseDataDbTableColumn() {
        return courseDataDbTableColumn;
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
            _identType = IdentType.valueOf(nv);
        });
        representation.addListener((o, ov, nv ) -> {
            isChanged = true;
            _representation = nv;
        });
    }
}
