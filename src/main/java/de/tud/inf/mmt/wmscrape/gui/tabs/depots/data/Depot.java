package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data.Account;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@IdClass(DepotKey.class)
public class Depot {

    @Id
    @GeneratedValue
    private int id;
    @Id
    private int accountId;

    private String name;
    @Column(name = "Öffnungsdatum")
    private Date opened;
    @Column(name = "Schließungsdatum")
    private Date closed;
    @ManyToOne(fetch=FetchType.LAZY)

    @JoinColumn(name = "accountId", referencedColumnName = "id", updatable = false, insertable = false)
    private Account account;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="depot",  orphanRemoval = true)
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    public Depot() {
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
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
