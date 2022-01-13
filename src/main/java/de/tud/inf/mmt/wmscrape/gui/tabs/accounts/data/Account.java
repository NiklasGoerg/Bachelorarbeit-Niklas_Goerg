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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="account", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<AccountTransaction> accountTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="account", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<Depot> depots = new ArrayList<>();

    @Column(name = "kontonummer", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String identificationNumber;

    @Column(name = "eigentümer", columnDefinition = "TEXT")
    private String owner;

    @Column(name = "öffnungsdatum")
    private Date opened;

    @Column(name = "schließungsdatum")
    private Date closed;

    @Enumerated(EnumType.STRING)
    @Column(name = "kontotyp", nullable = false)
    private AccountType accountType;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public Account() {}

    public Account(String identificationNumber, AccountType accountType) {
        this.identificationNumber = identificationNumber;
        this.accountType = accountType;
    }

}
