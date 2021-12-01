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
public class StockColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "stockColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    @OneToMany(mappedBy = "stockColumn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ElementIdentCorrelation> elementIdentCorrelations;

    public StockColumn() {}

    public StockColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }

    @Override
    public String getTableName() {
        return StockTableManager.TABLE_NAME;
    }
}
