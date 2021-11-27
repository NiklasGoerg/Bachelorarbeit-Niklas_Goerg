package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TransactionTableManager extends DbTableManger {

    public static final String TABLE_NAME = "depottransaktion";
    public static final List<String> RESERVED_COLUMNS = List.of("datum", "isin");
    public static final List<String> COLUMN_ORDER = List.of("datum", "isin");

    @Autowired
    TransactionColumnRepository transactionColumnRepository;

    @PostConstruct
    private void initTransactionData() {
        // table is created by spring
        initTableColumns(transactionColumnRepository, TABLE_NAME);
    }

    @Override
    public boolean removeColumn(String columnName) {
        throw new NotImplementedFunctionException("This table is managed by hibernate");
    }

    @Override
    public void addColumn(String colName, ColumnDatatype datatype) {
        throw new NotImplementedFunctionException("This table is managed by hibernate");
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
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
