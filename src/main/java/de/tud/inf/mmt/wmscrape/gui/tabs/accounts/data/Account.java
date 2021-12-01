package de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "konto")
public class Account {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "kontonummer")
    private String identificationNumber;

    @Column(name = "eigentümer")
    private String owner;

    @Column(name = "öffnungsdatum")
    private Date opened;

    @Column(name = "schließungsdatum")
    private Date closed;

    @Enumerated(EnumType.STRING)
    @Column(name = "kontotyp")
    private AccountType accountType;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="account", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<AccountTransaction> accountTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="account", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<Depot> depots = new ArrayList<>();

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
