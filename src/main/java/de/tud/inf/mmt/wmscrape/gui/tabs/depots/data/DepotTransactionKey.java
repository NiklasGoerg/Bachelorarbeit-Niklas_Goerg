package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * one of two possibilities used by hibernate to allow composite primary keys
 */
@SuppressWarnings("unused")
public class DepotTransactionKey implements Serializable {
    private String depotName;
    private Date date;
    private String stockIsin;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepotTransactionKey that = (DepotTransactionKey) o;
        return Objects.equals(depotName, that.depotName) && Objects.equals(date, that.date) && Objects.equals(stockIsin, that.stockIsin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depotName, date, stockIsin);
    }
}
