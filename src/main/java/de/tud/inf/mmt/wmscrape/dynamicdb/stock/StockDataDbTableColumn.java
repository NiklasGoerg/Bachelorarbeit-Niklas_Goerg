package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("S")
public class StockDataDbTableColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stockDataTableColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    @OneToMany(mappedBy = "stockDataTableColumn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ElementIdentCorrelation> elementIdentCorrelations;


    public StockDataDbTableColumn() {}

    public StockDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }

    @Override
    public String getTableName() {
        return StockDataDbManager.TABLE_NAME;
    }
}
