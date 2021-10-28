package de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private int id;
    @Column(name = "Kontonummer")
    private String identificationNumber;
    @Column(name = "Eigentümer")
    private String owner;
    @Column(name = "Öffnungsdatum")
    private Date opened;
    @Column(name = "Schließungsdatum")
    private Date closed;
    @Enumerated(EnumType.STRING)
    @Column(name = "Kontotyp")
    private AccountType accountType;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="account", orphanRemoval = true)
    private List<AccountTransaction> accountTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="account", orphanRemoval = true)
    private List<Depot> depots = new ArrayList<>();

    public Account() {
    }

    public Account(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
