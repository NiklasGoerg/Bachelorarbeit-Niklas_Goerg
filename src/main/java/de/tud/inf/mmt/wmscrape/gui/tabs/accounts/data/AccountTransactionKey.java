package de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * one of two possibilities used by hibernate to allow composite primary keys
 */
public class AccountTransactionKey implements Serializable {
    private int accountId;
    private int id;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTransactionKey that = (AccountTransactionKey) o;
        return accountId == that.accountId && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, id);
    }
}
