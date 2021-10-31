package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;

import javax.persistence.*;

@Entity
public class ExcelCorrelation {

    @Id
    @GeneratedValue
    private int id;
    private String dbColTitle;
    private String excelColTitle;
    private int excelColNumber;
    @Enumerated(EnumType.STRING)
    private CorrelationType correlationType;

    public ExcelCorrelation() {
    }

    public ExcelCorrelation(String excelColTitle, String dbColTitle) {
        this.excelColTitle = excelColTitle;
        this.dbColTitle = dbColTitle;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="excelSheetId", referencedColumnName="id")
    private ExcelSheet excelSheet;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="stockDataTableColumnId", referencedColumnName="id")
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

    public String getExcelColTitle() {
        return excelColTitle;
    }

    public void setExcelColTitle(String excelColTitle) {
        this.excelColTitle = excelColTitle;
    }

    public int getExcelColNumber() {
        return excelColNumber;
    }

    public void setExcelColNumber(int excelColNumber) {
        this.excelColNumber = excelColNumber;
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
