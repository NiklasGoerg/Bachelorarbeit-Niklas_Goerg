package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

public class DepotTransactionKey implements Serializable {
    private int id;
    private int depotId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepotTransactionKey that = (DepotTransactionKey) o;
        return id == that.id && depotId == that.depotId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, depotId);
    }
}
