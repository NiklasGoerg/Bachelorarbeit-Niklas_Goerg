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

    @Column(name = "öffnungsdatum")
    private Date opened;

    @Column(name = "schließungsdatum")
    private Date closed;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="depot",  orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    public Depot() {
    }

//    public int getId() {
//        return id;
//    }

    public Depot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getOpened() {
        return opened;
    }

    public void setOpened(Date opened) {
        this.opened = opened;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<DepotTransaction> getDepotTransactions() {
        return depotTransactions;
    }

    public void setDepotTransactions(List<DepotTransaction> depotTransactions) {
        this.depotTransactions = depotTransactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Depot depot)) return false;
        return Objects.equals(name, depot.name) && Objects.equals(opened, depot.opened) && Objects.equals(closed, depot.closed);
    }
}
