package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "depottransaktion";
    public static final List<String> RESERVED_COLUMNS = List.of("datum", "isin");
    public static final List<String> COLUMN_ORDER = List.of("datum", "isin");

    @Autowired
    TransactionDataColumnRepository transactionDataColumnRepository;

    @PostConstruct
    private void initTransactionData() {

        // the column names where a representation in db_table_column_exists
        ArrayList<String> representedColumns = new ArrayList<>();
        for(TransactionDataDbTableColumn column : transactionDataColumnRepository.findAll()) {
            representedColumns.add(column.getName());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!representedColumns.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                if(datatype == null) continue;
                transactionDataColumnRepository.saveAndFlush(new TransactionDataDbTableColumn(colName, datatype));
            } else {
                // representation exists
                representedColumns.remove(colName);
            }
        }

        // removing references that do not exist anymore
        removeOldRepresentation(representedColumns, transactionDataColumnRepository);
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

}
