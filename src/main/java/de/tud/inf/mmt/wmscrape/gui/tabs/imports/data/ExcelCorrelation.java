package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumn;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

@Entity
public class ExcelCorrelation {

    @Id
    @GeneratedValue
    private int id;
    private String dbColTitle;
    @Enumerated(EnumType.STRING)
    private ColumnDatatype dbColType;

    @Column(name = "excelColTitle")
    private String _excelColTitle;
    @Transient
    private final SimpleStringProperty excelColTitle = new SimpleStringProperty();

    @Column(name = "excelColNumber")
    private int _excelColNumber = -1;
    @Transient
    private final SimpleIntegerProperty excelColNumber = new SimpleIntegerProperty();

    @Enumerated(EnumType.STRING)
    private CorrelationType correlationType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="excelSheetId", referencedColumnName="id")
    private ExcelSheet excelSheet;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stockColumnId", referencedColumnName="id")
    private StockColumn stockColumn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transactionColumnId", referencedColumnName="id")
    private TransactionColumn transactionColumn;

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
