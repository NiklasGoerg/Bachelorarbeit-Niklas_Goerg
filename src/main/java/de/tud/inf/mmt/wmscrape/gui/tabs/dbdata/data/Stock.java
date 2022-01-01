package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotTransaction;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "wertpapier")
public class Stock {

    @Id
    @Column(length = 50)
    private String isin;

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<DepotTransaction> depotTransactions = new ArrayList<>();

    @OneToMany(fetch=FetchType.LAZY, mappedBy ="stock", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ElementSelection> elementSelections = new ArrayList<>();

    @Column(name = "wkn",columnDefinition = "TEXT")
    private String _wkn;
    @Column(name = "name",columnDefinition = "TEXT")
    private String _name;
    @Column(name = "typ",columnDefinition = "TEXT")
    private String _stockType;


    @Transient
    private final SimpleStringProperty wkn = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty name = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty stockType = new SimpleStringProperty();

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
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

    public SimpleStringProperty stockTypeProperty() {
        return stockType;
    }

    /**
     * due to the fact that hibernate creates proxies (ubclasses of the actual entities) one has to use "instanceof" to compare
     * objects. normally checking of equality can cause unexpected results.
     * lazy loaded fields are omitted because one can not know if a session is still attached.
     *
     * @param o the object to compare to
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock stock)) return false;
        return Objects.equals(isin, stock.isin);
    }

    /**
     * called after entity creation by hibernate (loading from the database)
     * updates the property values to those from the database
     */
    @PostLoad
    private void setPropertiesFromPersistence() {
        name.set(_name);
        wkn.set(_wkn);
        stockType.set(_stockType);
        initListener();
    }

    /**
     * allows using properties which can't be stored by hibernate.
     * when a property changes the filed inside the entity changes which can be stored as usual
     */
    private void initListener() {
        wkn.addListener((o,ov,nv) -> _wkn = nv.trim());
        name.addListener((o,ov,nv) -> _name = nv.trim());
        stockType.addListener((o,ov,nv) -> _stockType = nv.trim());

    }

}
