package de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@IdClass(AccountTransactionKey.class)
@Table(name = "Kontotransaktion")
public class AccountTransaction {

    @Id
    @GeneratedValue
    private int id;
    @Id
    private int accountId;

    @Column(name = "Zeitpunkt")
    private LocalDateTime timestamp;
    @Column(name = "Transaktionswert")
    private double amount;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="accountId", referencedColumnName="id", updatable=false, insertable=false)
    private Account account;

    public int getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Enumerated
    @Column(name = "Typ")
    private TransactionType transactionType;

}
