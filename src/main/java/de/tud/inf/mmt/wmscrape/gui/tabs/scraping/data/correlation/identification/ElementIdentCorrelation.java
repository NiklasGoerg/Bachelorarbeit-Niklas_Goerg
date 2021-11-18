package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
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
    private IdentType _identType = IdentType.DEAKTIVIERT;
    @Transient
    private final SimpleStringProperty identType = new SimpleStringProperty(_identType.name());
    @Column(name = "identification")
    private String _identification;
    @Transient
    private final SimpleStringProperty identification = new SimpleStringProperty();

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

    private ColumnDatatype columnDatatype;

    // optional for exchange correlations
    private String exchangeFieldName;

    @Transient
    private boolean isChanged = false;

    @PostLoad
    private void setPropertiesFromPersistence() {
        identType.set(_identType.name());
        identification.set(_identification);
        initListener();
    }

    public ElementIdentCorrelation() {}

    private ElementIdentCorrelation(WebsiteElement websiteElement) {
        this.websiteElement = websiteElement;
        initListener();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, StockDataDbTableColumn stockDataTableColumn) {
        this(websiteElement);
        this.stockDataTableColumn = stockDataTableColumn;
        this.columnDatatype = stockDataTableColumn.getColumnDatatype();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, CourseDataDbTableColumn courseDataDbTableColumn) {
        this(websiteElement);
        this.courseDataDbTableColumn = courseDataDbTableColumn;
        this.columnDatatype = courseDataDbTableColumn.getColumnDatatype();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, String exchangeFieldName) {
        this.websiteElement = websiteElement;
        this.exchangeFieldName = exchangeFieldName;
        this.columnDatatype = ColumnDatatype.DOUBLE;
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

    public String getIdentification() {
        return identification.get();
    }

    public void setIdentification(String identification) {
        this.identification.set(identification);
    }

    public SimpleStringProperty identificationProperty() {
        return identification;
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

    public ColumnDatatype getColumnDatatype() {
        return columnDatatype;
    }

    public boolean isChanged() {
        return isChanged;
    }

    private void initListener() {
        identType.addListener((o, ov, nv) -> {
            isChanged = true;
            _identType = IdentType.valueOf(nv);
        });
        identification.addListener((o, ov, nv ) -> {
            isChanged = true;
            _identification = nv;
        });
    }
}
