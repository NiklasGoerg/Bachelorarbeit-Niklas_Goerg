package de.tud.inf.mmt.wmscrape.gui.tabs.data.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.TableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class StockDataTableColumn extends TableColumn {

    public StockDataTableColumn() {}

    public StockDataTableColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stockDataTableColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    public void setExcelCorrelations(List<ExcelCorrelation> excelCorrelations) {
        this.excelCorrelations = excelCorrelations;
    }
}
