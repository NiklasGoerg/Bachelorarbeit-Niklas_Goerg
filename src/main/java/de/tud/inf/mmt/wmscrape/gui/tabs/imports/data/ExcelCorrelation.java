package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumn;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

@Entity
@Table(name = "excel_spaltenzuordnung")
public class ExcelCorrelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="excel_sheet_id", referencedColumnName="id", updatable = false, nullable = false)
    private ExcelSheet excelSheet;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stock_column_id", referencedColumnName="id", updatable = false)
    private StockColumn stockColumn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_column_id", referencedColumnName="id", updatable = false)
    private TransactionColumn transactionColumn;


    @Column(name = "db_col_title", updatable = false, nullable = false)
    private String dbColTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "db_col_type", updatable = false, nullable = false)
    private ColumnDatatype dbColType;

    @Column(name = "excel_col_title")
    private String _excelColTitle;

    @Column(name = "excel_col_number", nullable = false)
    private int _excelColNumber = -1;

    @Enumerated(EnumType.STRING)
    @Column(name = "correlation_type", nullable = false, updatable = false)
    private CorrelationType correlationType;


    @Transient
    private final SimpleStringProperty excelColTitle = new SimpleStringProperty();
    @Transient
    private final SimpleIntegerProperty excelColNumber = new SimpleIntegerProperty();


    public ExcelCorrelation() {
        excelColNumber.set(_excelColNumber);
        initListener();
    }

    public ExcelCorrelation(CorrelationType correlationType, ExcelSheet excelSheet) {
        this();
        this.correlationType = correlationType;
        this.excelSheet = excelSheet;
    }

    public ExcelCorrelation(CorrelationType correlationType, ExcelSheet excelSheet, StockColumn column) {
        this(correlationType, excelSheet);
        this.dbColType = column.getColumnDatatype();
        this.dbColTitle = column.getName();
        this.stockColumn = column;
    }

    public ExcelCorrelation(CorrelationType correlationType, ExcelSheet excelSheet, TransactionColumn column) {
        this(correlationType, excelSheet);
        this.dbColType = column.getColumnDatatype();
        this.dbColTitle = column.getName();
        this.transactionColumn = column;
    }

    public ExcelCorrelation(CorrelationType correlationType, ExcelSheet excelSheet, ColumnDatatype columnDatatype, String colName) {
        this(correlationType, excelSheet);
        this.dbColType = columnDatatype;
        this.dbColTitle = colName;
    }

    public int getId() {
        return id;
    }

    public String getDbColTitle() {
        return dbColTitle;
    }

    public String getExcelColTitle() {
        return excelColTitle.get();
    }

    public SimpleStringProperty excelColTitleProperty() {
        return excelColTitle;
    }

    public int getExcelColNumber() {
        return excelColNumber.get();
    }

    public void setExcelColNumber(int excelColNumber) {
        this.excelColNumber.set(excelColNumber);
    }

    public void setExcelColTitle(String excelColTitle) {
        this.excelColTitle.set(excelColTitle);
    }

    public CorrelationType getCorrelationType() {
        return correlationType;
    }

    public ColumnDatatype getDbColDataType() {
        return dbColType;
    }

    @PostLoad
    private void setPropertiesFromPersistence() {
        excelColTitle.set(_excelColTitle);
        excelColNumber.set(_excelColNumber);
        initListener();
    }

    private void initListener() {
        excelColTitle.addListener((o, ov, nv ) -> _excelColTitle = nv);
        excelColNumber.addListener((o, ov, nv ) -> _excelColNumber = (int) nv);
    }
}
