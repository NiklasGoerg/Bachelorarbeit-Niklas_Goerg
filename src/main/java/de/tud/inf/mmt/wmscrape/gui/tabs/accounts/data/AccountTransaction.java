package de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@IdClass(AccountTransactionKey.class)
@Table(name = "konto_transaktion")
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Id
    @Column(name = "account_id")
    private int accountId;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="account_id", referencedColumnName="id", updatable=false, insertable=false, nullable = false)
    private Account account;

    @Column(name = "zeitpunkt")
    private LocalDateTime timestamp;

    @Column(name = "transaktionswert")
    private double amount;

    @Enumerated
    @Column(name = "typ", nullable = false, updatable = false)
    private TransactionType transactionType;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public AccountTransaction() {}

    public AccountTransaction(Account account, TransactionType transactionType) {
        this.account = account;
        this.transactionType = transactionType;
    }

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

}
