package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.Stock;

import javax.persistence.*;
import java.sql.Date;

@Entity
@IdClass(DepotTransactionKey.class)
@Table(name = "Depottransaktion")
public class DepotTransaction {
    @Id
    private int depotId;

    @Id
    @Column(name = "transaktionsDatum")
    private Date date;

    @Id
    @Column(name = "wertpapierIsin", length = 50)
    private String stockIsin;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="depotId", referencedColumnName="id", updatable=false, insertable=false)
    private Depot depot;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="wertpapierIsin", referencedColumnName="isin", updatable=false, insertable=false)
    private Stock stock;

    //@Enumerated(EnumType.STRING)
    @Column(name = "transaktionstyp")
    private String transactionType;

    @Column(name = "anzahl")
    private int amount;

    //@Enumerated(EnumType.STRING)
    @Column(name = "währung")
    private String currency;

    @Column(name = "preis")
    private double price;

    @Column(name = "wertInEur")
    private double priceInEur;

    @Column(name = "bankprovision")
    private double bankProvision;

    @Column(name = "maklercourtage")
    private double commission;

    @Column(name = "börsenplatzgebühr")
    private double brokerFees;

    @Column(name = "spesen")
    private double fees;

    @Column(name = "kapitalertragssteuer")
    private double capitalYieldsTax;

    @Column(name = "solidaritätssteuer")
    private double soliditarySurcharge;

    @Column(name = "quellensteuer")
    private double witholdingTax;

    @Column(name = "abgeltungssteuer")
    private double flatTax;

    @Column(name = "kirchensteuer")
    private double churchTax;


    public DepotTransaction() {
    }

    public DepotTransaction(Date date, Stock stock, Depot depot) {
        this.depotId = depot.getId();
        this.date = date;
        this.stock = stock;
        this.depot = depot;
        this.stockIsin = stock.getIsin();
    }

    public Depot getDepot() {
        return depot;
    }

    public String getStockIsin() {
        return stockIsin;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Date getDate() {
        return date;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceInEur() {
        return priceInEur;
    }

    public void setPriceInEur(double priceInEur) {
        this.priceInEur = priceInEur;
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
