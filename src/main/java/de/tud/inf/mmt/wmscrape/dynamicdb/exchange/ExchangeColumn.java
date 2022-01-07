package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("E")
public class ExchangeColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "exchangeColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ElementSelection> elementSelections = new ArrayList<>();

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public ExchangeColumn() {}

    public ExchangeColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }

    public ExchangeColumn(String name, VisualDatatype visualDatatype) {
        super(name, visualDatatype);
    }

    @Override
    public String getTableName() {
        return ExchangeTableManager.TABLE_NAME;
    }
}
