package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("T")
public class TransactionColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "transactionColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public TransactionColumn() {}

    public TransactionColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }


    @Override
    public String getTableName() {
        return TransactionTableManager.TABLE_NAME;
    }
}
