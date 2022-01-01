package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

/**
 * stores the xpath / css / etc. data for one website element configuration
 */
@Entity
@Table(name = "webseiten_element_identifikation")
public class ElementIdentCorrelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "website_element_id", referencedColumnName = "id", updatable = false, nullable = false)
    private WebsiteElement websiteElement;

    // optional for stock correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_column_id", referencedColumnName = "id", updatable = false)
    private StockColumn stockColumn;

    // optional for course correlations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_column_id", referencedColumnName = "id", updatable = false)
    private CourseColumn courseColumn;

    @Column(name = "ident_type", columnDefinition = "TEXT", nullable = false)
    @Enumerated(EnumType.STRING)
    private IdentType _identType = IdentType.DEAKTIVIERT;

    @Column(name = "identification", columnDefinition = "TEXT")
    private String _identification;

    @Column(name = "regex", columnDefinition = "TEXT")
    private String _regex;

    // redundant but saves fetching from the database
    @Column(name = "column_datatype", updatable = false, nullable = false)
    private ColumnDatatype columnDatatype;

    @Column(name = "db_col_name", updatable = false, nullable = false)
    private String dbColName;

    @Column(name = "db_table_name", updatable = false, nullable = false)
    private String dbTableName;

    @Transient
    private final SimpleStringProperty identification = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty regex = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty identType = new SimpleStringProperty(_identType.name());
    @Transient
    private boolean isChanged = false;


    /**
     * called after entity creation by hibernate (loading from the database)
     * updates the property values to those from the database
     */
    @PostLoad
    private void setPropertiesFromPersistence() {
        identType.set(_identType.name());
        identification.set(_identification);
        regex.set(_regex);
        initListener();
    }

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public ElementIdentCorrelation() {}

    /**
     * do not use this constructor
     * @param websiteElement the corresponding website element configuration
     */
    private ElementIdentCorrelation(WebsiteElement websiteElement) {
        this.websiteElement = websiteElement;
        initListener();
    }

    /**
     * @param websiteElement the corresponding website element configuration
     * @param stockColumn the database column the correlation relates to in form of a column entity
     */
    public ElementIdentCorrelation(WebsiteElement websiteElement, StockColumn stockColumn) {
        this(websiteElement);
        this.stockColumn = stockColumn;
        this.columnDatatype = stockColumn.getColumnDatatype();
        this.dbColName = stockColumn.getName();
        this.dbTableName = stockColumn.getTableName();
    }

    /**
     * @param websiteElement the corresponding website element configuration
     * @param courseColumn the database column the correlation relates to in form of a column entity
     */
    public ElementIdentCorrelation(WebsiteElement websiteElement, CourseColumn courseColumn) {
        this(websiteElement);
        this.courseColumn = courseColumn;
        this.columnDatatype = courseColumn.getColumnDatatype();
        this.dbColName = courseColumn.getName();
        this.dbTableName = courseColumn.getTableName();
    }

    /**
     * used for some special columns
     *
     * @param websiteElement the corresponding website element configuration
     * @param colName the database column the correlation relates to in form of the column name
     */
    public ElementIdentCorrelation(WebsiteElement websiteElement, ColumnDatatype datatype, String dbTableName, String colName) {
        this(websiteElement);
        this.dbColName = colName;
        this.columnDatatype = datatype;
        this.dbTableName = dbTableName;
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

    /**
     * allows using properties which can't be stored by hibernate.
     * when a property changes the filed inside the entity changes which can be stored as usual
     */
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
