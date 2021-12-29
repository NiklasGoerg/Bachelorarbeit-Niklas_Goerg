package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

@Entity
@Table(name = "webseiten_element_identifikation")
public class ElementIdentCorrelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ident_type", columnDefinition = "TEXT")
    @Enumerated(EnumType.STRING)
    private IdentType _identType = IdentType.DEAKTIVIERT;

    @Column(name = "identification", columnDefinition = "TEXT")
    private String _identification;

    @Column(name = "regex", columnDefinition = "TEXT")
    private String _regex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "website_element_id", referencedColumnName = "id")
    private WebsiteElement websiteElement;

    // optional for stock correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_column_id", referencedColumnName = "id")
    private StockColumn stockColumn;

    // optional for course correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_column_id", referencedColumnName = "id")
    private CourseColumn courseColumn;

    // redundant but saves fetching from the database
    @Column(name = "column_datatype")
    private ColumnDatatype columnDatatype;

    @Column(name = "db_col_name")
    private String dbColName;

    @Column(name = "db_table_name")
    private String dbTableName;

    @Transient
    private final SimpleStringProperty identification = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty regex = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty identType = new SimpleStringProperty(_identType.name());
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

    public ElementIdentCorrelation(WebsiteElement websiteElement, StockColumn stockColumn) {
        this(websiteElement);
        this.stockColumn = stockColumn;
        this.columnDatatype = stockColumn.getColumnDatatype();
        this.dbColName = stockColumn.getName();
        this.dbTableName = stockColumn.getTableName();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, CourseColumn courseColumn) {
        this(websiteElement);
        this.courseColumn = courseColumn;
        this.columnDatatype = courseColumn.getColumnDatatype();
        this.dbColName = courseColumn.getName();
        this.dbTableName = courseColumn.getTableName();
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, String exchangeFieldName) {
        this(websiteElement);
        this.dbColName = exchangeFieldName;
        this.columnDatatype = ColumnDatatype.DOUBLE;
        this.dbTableName = ExchangeTableManager.TABLE_NAME;
    }

    public ElementIdentCorrelation(WebsiteElement websiteElement, ColumnDatatype datatype, String colName) {
        this(websiteElement);
        this.dbColName = colName;
        this.columnDatatype = datatype;
        this.dbTableName = CourseTableManager.TABLE_NAME;
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
            _identification = nv.trim();
        });
        regex.addListener((o, ov, nv ) -> {
            isChanged = true;
            _regex = nv.trim();
        });
    }
}
