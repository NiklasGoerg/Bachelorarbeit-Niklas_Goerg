package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("E")
public class ExchangeDataDbTableColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "exchangeDataDbTableColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ElementSelection> elementSelections = new ArrayList<>();

    public ExchangeDataDbTableColumn() {}

    public ExchangeDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }

    @Override
    public String getTableName() {
        return ExchangeDataDbManager.TABLE_NAME;
    }
}
