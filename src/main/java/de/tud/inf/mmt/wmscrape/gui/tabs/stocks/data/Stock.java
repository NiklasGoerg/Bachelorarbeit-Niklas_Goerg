package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransaction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Wertpapier")
public class Stock {

    @Id
    @Column(length = 50)
    private String isin;
    private String wkn;
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "typ")
    private StockType stockType;
    @Column(name = "gruppenId")
    private int groupId;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock")
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    public Stock() {
    }

    public Stock(String isin) {
        this.isin = isin;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getWkn() {
        return wkn;
    }

    public void setWkn(String wkn) {
        this.wkn = wkn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StockType getStockType() {
        return stockType;
    }

    public void setStockType(StockType stockType) {
        this.stockType = stockType;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public List<DepotTransaction> getDepotTransactions() {
        return depotTransactions;
    }

    public void addDepotTransaction(DepotTransaction depotTransaction) {
        this.depotTransactions.add(depotTransaction);
    }
}
