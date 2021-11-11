package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.ElementCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class StockDataTableColumn {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ColumnDatatype columnDatatype;

    public StockDataTableColumn() {
    }

    public StockDataTableColumn(String name, ColumnDatatype columnDatatype) {
        this.name = name;
        this.columnDatatype = columnDatatype;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stockDataTableColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stockDataTableColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementCorrelation> elementCorrelations = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnDatatype getColumnDatatype() {
        return columnDatatype;
    }

    public void setColumnDatatype(ColumnDatatype columnDatatype) {
        this.columnDatatype = columnDatatype;
    }

    public int getId() {
        return id;
    }

    public void setExcelCorrelations(List<ExcelCorrelation> excelCorrelations) {
        this.excelCorrelations = excelCorrelations;
    }
}
