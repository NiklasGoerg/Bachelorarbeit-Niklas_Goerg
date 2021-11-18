package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.data.ExcelCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("T")
public class TransactionDataDbTableColumn extends DbTableColumn {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "transactionDataTableColumn",  orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ExcelCorrelation> excelCorrelations = new ArrayList<>();


    public TransactionDataDbTableColumn() {}

    public TransactionDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
        super(name, columnDatatype);
    }


    @Override
    public String getTableName() {
        return TransactionDataDbManager.TABLE_NAME;
    }
}
