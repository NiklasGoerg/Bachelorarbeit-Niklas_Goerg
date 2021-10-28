package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransaction;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class StockDataTableColumn {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private ColumnDatatype columnDatatype;

    @OneToMany(fetch= FetchType.LAZY, mappedBy ="stockDataTableColumn",  orphanRemoval = true)
    private List<ExcelCorrelation> excelCorrelations = new ArrayList<>();
}
