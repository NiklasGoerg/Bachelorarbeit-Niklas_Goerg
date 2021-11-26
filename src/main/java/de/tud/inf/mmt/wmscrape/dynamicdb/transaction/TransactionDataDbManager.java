package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class TransactionDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "depottransaktion";
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
        removeRepresentation(representedColumns, transactionDataColumnRepository);
    }

    @Override
    public boolean removeColumn(String columnName) {
        throw new NotImplementedFunctionException("This table is managed by spring");
    }

    @Override
    protected void addColumn(String colName, ColumnDatatype datatype) {
        throw new NotImplementedFunctionException("This table is managed by spring");
    }

}
