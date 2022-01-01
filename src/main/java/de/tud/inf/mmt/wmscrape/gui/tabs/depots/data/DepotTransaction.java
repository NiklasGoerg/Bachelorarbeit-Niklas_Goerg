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

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="depot_name", referencedColumnName="name", updatable=false, nullable = false, insertable = false)
    private Depot depot;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name="wertpapier_isin", referencedColumnName="isin", updatable=false, nullable = false, insertable = false)
    private Stock stock;

    @Column(name = "transaktionstyp", columnDefinition = "TEXT")
    private String transactionType;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public DepotTransaction() {}

    public DepotTransaction(Date date, Depot depot, Stock stock) {
        this.date = date;
        this.depot = depot;
        this.stock = stock;
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
