package de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.TableColumn;

import javax.persistence.Entity;

@Entity
public class ExchangeDataTableColumn extends TableColumn {

        public ExchangeDataTableColumn() {}

        public ExchangeDataTableColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
        }
}
