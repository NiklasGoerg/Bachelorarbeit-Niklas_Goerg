package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data.Account;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "depot")
public class Depot {
    @Id
    @Column(length = 500)
    private String name;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="depot",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<DepotTransaction> depotTransactions = new ArrayList<>();

    // depot doesn't have to have an account atm
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @Column(name = "öffnungsdatum")
    private Date opened;

    @Column(name = "schließungsdatum")
    private Date closed;

    public Depot() {
    }

    public Depot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    /**
     * due to the fact that hibernate creates proxies (subclasses of the actual entities) one has to use "instanceof" to compare
     * objects. normally checking of equality can cause unexpected results.
     * lazy loaded fields are omitted because one can not know if a session is still attached.
     *
     * @param o the object to compare to
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Depot depot)) return false;
        return Objects.equals(name, depot.name) && Objects.equals(opened, depot.opened) && Objects.equals(closed, depot.closed);
    }
}
