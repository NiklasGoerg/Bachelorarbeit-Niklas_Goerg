package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import java.io.Serializable;
import java.util.Objects;

public class DepotKey implements Serializable {
    private int id;
    private int accountId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepotKey depotKey = (DepotKey) o;
        return id == depotKey.id && accountId == depotKey.accountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId);
    }
}
