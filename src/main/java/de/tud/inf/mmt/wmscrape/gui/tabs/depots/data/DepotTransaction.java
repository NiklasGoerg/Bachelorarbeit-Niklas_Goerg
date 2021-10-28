package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.Stock;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@IdClass(DepotTransactionKey.class)
@Table(name = "Depottransaktion")
public class DepotTransaction {
    @Id
    @GeneratedValue
    private int id;
    @Id
    private int depotId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="depotId", referencedColumnName="id", updatable=false, insertable=false)
    @JoinColumn(name = "depotAccountId", referencedColumnName="accountId", updatable=false, insertable=false)
    private Depot depot;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="wertpapierIsin", referencedColumnName="isin")
    private Stock stock;

    @Column(name = "Zeitpunkt")
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    @Column(name = "Transaktionstyp")
    private TransactionType transactionType;
    @Column(name = "Anzahl")
    private int amount;
    @Enumerated(EnumType.STRING)
    @Column(name = "Währung")
    private Currency currency;
    @Column(name = "Preis")
    private double price;
    @Column(name = "WertInEur")
    private double amountInEur;
    @Column(name = "Bankprovision")
    private double bankProvision;
    @Column(name = "Maklercourtage")
    private double commission;
    @Column(name = "Börsenplatzgebühr")
    private double brokerFees;
    @Column(name = "Spesen")
    private double fees;
    @Column(name = "Kapitalertragssteuer")
    private double capitalYieldsTax;
    @Column(name = "Solidaritätssteuer")
    private double soliditarySurcharge;
    @Column(name = "Quellensteuer")
    private double witholdingTax;
    @Column(name = "Abgeltungssteuer")
    private double flatTax;
    @Column(name = "Kirchensteuer")
    private double churchTax;


    public int getId() {
        return id;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public int getDepotId() {
        return depotId;
    }

    public void setDepotId(int depotId) {
        this.depotId = depotId;
    }


    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmountInEur() {
        return amountInEur;
    }

    public void setAmountInEur(double amountInEur) {
        this.amountInEur = amountInEur;
    }

    public double getBankProvision() {
        return bankProvision;
    }

    public void setBankProvision(double bankProvision) {
        this.bankProvision = bankProvision;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getBrokerFees() {
        return brokerFees;
    }

    public void setBrokerFees(double brokerFees) {
        this.brokerFees = brokerFees;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public double getCapitalYieldsTax() {
        return capitalYieldsTax;
    }

    public void setCapitalYieldsTax(double capitalYieldsTax) {
        this.capitalYieldsTax = capitalYieldsTax;
    }

    public double getSoliditarySurcharge() {
        return soliditarySurcharge;
    }

    public void setSoliditarySurcharge(double soliditarySurcharge) {
        this.soliditarySurcharge = soliditarySurcharge;
    }

    public double getWitholdingTax() {
        return witholdingTax;
    }

    public void setWitholdingTax(double witholdingTax) {
        this.witholdingTax = witholdingTax;
    }

    public double getFlatTax() {
        return flatTax;
    }

    public void setFlatTax(double flatTax) {
        this.flatTax = flatTax;
    }

    public double getChurchTax() {
        return churchTax;
    }

    public void setChurchTax(double churchTax) {
        this.churchTax = churchTax;
    }
}
