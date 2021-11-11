package de.tud.inf.mmt.wmscrape.gui.tabs.data.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransaction;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.ElementSelection;

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
    @Column(name = "typ")
    private String stockType;
    @Column(name = "gruppenId")
    private int groupId;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock")
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementSelection> elementSelections = new ArrayList<>();

    public Stock() {
    }

    public Stock(String isin, String wkn, String name, int groupId, String stockType) {
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
        this.groupId = groupId;
        this.stockType = stockType;
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

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
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

    public List<ElementSelection> getElementSelections() {
        return elementSelections;
    }

    public void setElementSelections(List<ElementSelection> elementSelections) {
        this.elementSelections = elementSelections;
    }
}
