package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;

import javax.persistence.*;
import java.sql.Date;

@Entity
@IdClass(DepotTransactionKey.class)
@Table(name = TransactionTableManager.TABLE_NAME)
public class DepotTransaction {
    @Id
    @Column(name="depot_name", length = 500)
    private String depotName;

    @Id
    @Column(name = "transaktions_datum")
    private Date date;

    @Id
    @Column(name = "wertpapier_isin", length = 50)
    private String stockIsin;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="depot_name", referencedColumnName="name", updatable=false, insertable=false)
    private Depot depot;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="wertpapier_isin", referencedColumnName="isin", updatable=false, insertable=false)
    private Stock stock;

    @Column(name = "transaktionstyp", columnDefinition = "TEXT")
    private String transactionType;

    public DepotTransaction() {
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
}
