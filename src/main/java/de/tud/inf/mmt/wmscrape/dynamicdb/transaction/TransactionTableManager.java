package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TransactionTableManager extends DbTableManger {

    public static final String TABLE_NAME = "depot_transaktion";
    public static final List<String> KEY_COLUMNS = List.of("depot_name", "transaktions_datum", "wertpapier_isin");
    public static final List<String> RESERVED_COLUMNS = List.of("depot_name", "transaktions_datum", "wertpapier_isin", "transaktionstyp");
    public static final List<String> COLUMN_ORDER = List.of("transaktions_datum", "depot_name", "wertpapier_isin", "transaktionstyp");

    @Autowired
    TransactionColumnRepository transactionColumnRepository;

    @PostConstruct
    private void initTransactionData() {
        // table is created by spring
        initTableColumns(transactionColumnRepository, TABLE_NAME);
    }

    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, transactionColumnRepository);
    }

    @Override
    public void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, transactionColumnRepository, new TransactionColumn(colName, datatype));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getKeyColumns() {
        return KEY_COLUMNS;
    }

    @Override
    public List<String> getReservedColumns() {
        return RESERVED_COLUMNS;
    }

    @Override
    public List<String> getColumnOrder() {
        return COLUMN_ORDER;
    }

    @Override
    protected void saveNewInRepository(String colName, ColumnDatatype datatype) {
        transactionColumnRepository.saveAndFlush(new TransactionColumn(colName, datatype));
    }

}
