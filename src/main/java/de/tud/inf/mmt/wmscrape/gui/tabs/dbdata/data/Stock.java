package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransaction;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

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
    @Column(name = "wkn")
    private String _wkn;
    @Column(name = "name",columnDefinition = "TEXT")
    private String _name;
    @Column(name = "typ")
    private String _stockType;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<DepotTransaction> depotTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementSelection> elementSelections = new ArrayList<>();

    @Transient
    private final SimpleStringProperty wkn = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty name = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty stockType = new SimpleStringProperty();
    @Transient
    private boolean isChanged = false;


    public Stock() {}

    public Stock(String isin, String wkn, String name, String stockType) {
        this.isin = isin;
        this._wkn = wkn;
        this._name = name;
        this._stockType = stockType;
        initListener();
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getWkn() {
        return wkn.get();
    }

    public SimpleStringProperty wknProperty() {
        return wkn;
    }

    public void setWkn(String wkn) {
        this.wkn.set(wkn);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getStockType() {
        return stockType.get();
    }

    public SimpleStringProperty stockTypeProperty() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType.set(stockType);
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
        return Objects.equals(isin, stock.isin);
    }

    @PostLoad
    private void setPropertiesFromPersistence() {
        name.set(_name);
        wkn.set(_wkn);
        stockType.set(_stockType);
        initListener();
    }

    private void initListener() {
        wkn.addListener((o,ov,nv) -> {
            isChanged = true;
            _wkn = nv.trim();
        });
        name.addListener((o,ov,nv) -> {
            isChanged = true;
            _name = nv.trim();
        });
        stockType.addListener((o,ov,nv) -> {
            isChanged = true;
            _stockType = nv.trim();
        });

    }

}
