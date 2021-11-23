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
        ArrayList<String> columnNames = new ArrayList<>();
        for(TransactionDataDbTableColumn column : transactionDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                transactionDataColumnRepository.save(new TransactionDataDbTableColumn(colName, datatype));
            }
        }
    }

    @Override
    public void removeColumn(String columnName) {
        throw new NotImplementedFunctionException("This table is managed by spring");
    }

}
