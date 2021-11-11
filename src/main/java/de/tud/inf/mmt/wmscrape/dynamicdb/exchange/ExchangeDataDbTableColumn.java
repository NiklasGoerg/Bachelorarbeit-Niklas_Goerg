package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;

import javax.persistence.Entity;

@Entity
public class ExchangeDataDbTableColumn extends DbTableColumn {

        public ExchangeDataDbTableColumn() {}

        public ExchangeDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
        }
}
