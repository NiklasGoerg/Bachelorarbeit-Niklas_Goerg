package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;

import javax.persistence.*;

@Entity
public class ExcelCorrelation {

    @Id
    @GeneratedValue
    private int id;

    private String dbColTitle;
    private CorrelationType correlationType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="excelSheetId", referencedColumnName="id", insertable=false, updatable = false)
    private ExcelSheet excelSheet;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stockDataTableColumnId", referencedColumnName="id", insertable=false, updatable = false)
    private StockDataTableColumn stockDataTableColumn;

    public int getId() {
        return id;
    }

    public String getDbColTitle() {
        return dbColTitle;
    }

    public void setDbColTitle(String dbColTitle) {
        this.dbColTitle = dbColTitle;
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

    public StockDataTableColumn getStockDataTableColumn() {
        return stockDataTableColumn;
    }

    public void setStockDataTableColumn(StockDataTableColumn stockDataTableColumn) {
        this.stockDataTableColumn = stockDataTableColumn;
    }
}
