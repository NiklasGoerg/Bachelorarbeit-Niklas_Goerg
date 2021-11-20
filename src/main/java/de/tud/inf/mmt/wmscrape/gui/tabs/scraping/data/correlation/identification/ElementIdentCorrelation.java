package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataDbManager;
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

    @Column(name = "identType", columnDefinition = "TEXT")
    @Enumerated(EnumType.STRING)
    private IdentType _identType = IdentType.DEAKTIVIERT;
    @Transient
    private final SimpleStringProperty identType = new SimpleStringProperty(_identType.name());

    @Column(name = "identification", columnDefinition = "TEXT")
    private String _identification;
    @Transient
    private final SimpleStringProperty identification = new SimpleStringProperty();

    @Column(name = "regex", columnDefinition = "TEXT")
    private String _regex;
    @Transient
    private final SimpleStringProperty regex = new SimpleStringProperty();

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

    // redundant but saves fetching from the database
    private ColumnDatatype columnDatatype;
    private String dbColName;
    private String dbTableName;


    @Transient
    private boolean isChanged = false;


    @PostLoad
    private void setPropertiesFromPersistence() {
        identType.set(_identType.name());
        identification.set(_identification);
        regex.set(_regex);
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
        this.dbColName = stockDataTableColumn.getName();
        this.dbTableName = stockDataTableColumn.getTableName();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, CourseDataDbTableColumn courseDataDbTableColumn) {
        this(websiteElement);
        this.courseDataDbTableColumn = courseDataDbTableColumn;
        this.columnDatatype = courseDataDbTableColumn.getColumnDatatype();
        this.dbColName = courseDataDbTableColumn.getName();
        this.dbTableName = courseDataDbTableColumn.getTableName();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, String exchangeFieldName) {
        this(websiteElement);
        this.dbColName = exchangeFieldName;
        this.columnDatatype = ColumnDatatype.DOUBLE;
        this.dbTableName = ExchangeDataDbManager.TABLE_NAME;
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

    public String getRegex() {
        return regex.get();
    }

    public SimpleStringProperty regexProperty() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex.set(regex);
    }

    public SimpleStringProperty identificationProperty() {
        return identification;
    }

    public WebsiteElement getWebsiteElement() {
        return websiteElement;
    }

    public ColumnDatatype getColumnDatatype() {
        return columnDatatype;
    }

    public String getDbColName() {
        return dbColName;
    }

    public String getDbTableName() {
        return dbTableName;
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
        regex.addListener((o, ov, nv ) -> {
            isChanged = true;
            _regex = nv;
        });
    }
}
