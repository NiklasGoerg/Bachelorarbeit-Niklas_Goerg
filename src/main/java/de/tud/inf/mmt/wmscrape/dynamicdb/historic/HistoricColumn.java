package de.tud.inf.mmt.wmscrape.dynamicdb.historic;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("H")
public class HistoricColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "historicColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ElementSelection> elementSelections = new ArrayList<>();

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public HistoricColumn() {}

    public HistoricColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }

    public HistoricColumn(String name, VisualDatatype visualDatatype) {
        super(name, visualDatatype);
    }

    @SuppressWarnings("unused")
    @Override
    public String getTableName() {
        return HistoricTableManager.TABLE_NAME;
    }
}
