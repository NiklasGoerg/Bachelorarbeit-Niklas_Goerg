package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data.Account;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Depot {

    @Id
    @GeneratedValue
    private int id;

    private String name;
    @Column(name = "Öffnungsdatum")
    private Date opened;
    @Column(name = "Schließungsdatum")
    private Date closed;
    @ManyToOne(fetch=FetchType.LAZY)

    @JoinColumn(name = "accountId", referencedColumnName = "id")
    private Account account;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="depot",  orphanRemoval = true)
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    public Depot() {
    }

    public int getId() {
        return id;
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

    public void addDepotTransaction(DepotTransaction depotTransaction) {
        this.depotTransactions.add(depotTransaction);
    }
}
