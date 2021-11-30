package de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@IdClass(AccountTransactionKey.class)
@Table(name = "kontotransaktion")
public class AccountTransaction {

    @Id
    @GeneratedValue
    private int id;
    @Id
    @Column(name = "account_id")
    private int accountId;

    @Column(name = "zeitpunkt")
    private LocalDateTime timestamp;

    @Column(name = "transaktionswert")
    private double amount;

    @Enumerated
    @Column(name = "typ")
    private TransactionType transactionType;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="account_id", referencedColumnName="id", updatable=false, insertable=false)
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

}
