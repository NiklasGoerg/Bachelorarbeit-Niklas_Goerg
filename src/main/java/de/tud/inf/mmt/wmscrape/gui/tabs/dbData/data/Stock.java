package de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransaction;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Wertpapier")
public class Stock {

    @Id
    @Column(length = 50)
    private String isin;
    private String wkn;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(name = "typ")
    private String stockType;

    @Column(name = "gruppenId")
    private int group;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementSelection> elementSelections = new ArrayList<>();

    public Stock() {
    }

    public Stock(String isin, String wkn, String name, int group, String stockType) {
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
        this.group = group;
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
        throw new NotImplementedFunctionException("Die Stammdatenansicht muss aufgrund der " +
                "Editierbarkeit vorher angepasst werden, damit es nicht zu inkonsistenzen kommt");
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        throw new NotImplementedFunctionException("Die Stammdatenansicht muss aufgrund der " +
                "Editierbarkeit vorher angepasst werden, damit es nicht zu inkonsistenzen kommt");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock)) return false;
        Stock stock = (Stock) o;
        return group == stock.group && Objects.equals(isin, stock.isin) && Objects.equals(wkn, stock.wkn) && Objects.equals(name, stock.name) && Objects.equals(stockType, stock.stockType);
    }
}
