package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

@Entity
public class ExcelCorrelation {

    @Id
    @GeneratedValue
    private int id;
    private String dbColTitle;
    @Column(name = "excelColTitle")
    private String _excelColTitle;
    @Transient
    private final SimpleStringProperty excelColTitle = new SimpleStringProperty();

    @Column(name = "excelColNumber")
    private int _excelColNumber = -1;
    @Transient
    private SimpleIntegerProperty excelColNumber = new SimpleIntegerProperty();
    @Enumerated(EnumType.STRING)
    private CorrelationType correlationType;

    public ExcelCorrelation() {
        initListener();
    }

    public ExcelCorrelation(String excelColTitle, String dbColTitle) {
        this.excelColTitle.set(excelColTitle);
        this._excelColTitle = excelColTitle;
        this.dbColTitle = dbColTitle;
        initListener();
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="excelSheetId", referencedColumnName="id")
    private ExcelSheet excelSheet;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stockDataTableColumnId", referencedColumnName="id")
    private StockDataDbTableColumn stockDataTableColumn;

    public int getId() {
        return id;
    }

    public String getDbColTitle() {
        return dbColTitle;
    }

    public void setDbColTitle(String dbColTitle) {
        this.dbColTitle = dbColTitle;
    }

    public String getExcelColTitle() {
        return excelColTitle.get();
    }

    public SimpleStringProperty excelColTitleProperty() {
        return excelColTitle;
    }

    public void setExcelColTitle(String excelColTitle) {
        this.excelColTitle.set(excelColTitle);
    }

    public int getExcelColNumber() {
        return excelColNumber.get();
    }

    public SimpleIntegerProperty excelColNumberProperty() {
        return excelColNumber;
    }

    public void setExcelColNumber(int excelColNumber) {
        this.excelColNumber.set(excelColNumber);
    }

    public CorrelationType getCorrelationType() {
        return correlationType;
    }

    public void setCorrelationType(CorrelationType correlationType) {
        this.correlationType = correlationType;
    }

    public ExcelSheet getExcelSheet() {
        return excelSheet;
    }

    public void setExcelSheet(ExcelSheet excelSheet) {
        this.excelSheet = excelSheet;
    }

    public StockDataDbTableColumn getStockDataTableColumn() {
        return stockDataTableColumn;
    }

    public void setStockDataTableColumn(StockDataDbTableColumn stockDataTableColumn) {
        this.stockDataTableColumn = stockDataTableColumn;
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
