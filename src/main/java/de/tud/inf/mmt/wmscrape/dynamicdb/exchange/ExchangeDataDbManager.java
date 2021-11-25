package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ExchangeDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "wechselkurse";
    @Autowired
    ExchangeDataColumnRepository exchangeDataColumnRepository;

    @PostConstruct
    private void initExchangeData() {
        // the exchange data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (datum DATE PRIMARY KEY);");
        }

        // the column names where a representation in db_table_column_exists
        ArrayList<String> representedColumns = new ArrayList<>();
        for(ExchangeDataDbTableColumn column : exchangeDataColumnRepository.findAll()) {
            representedColumns.add(column.getName());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!representedColumns.contains(colName)) {
                // add new representation
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                exchangeDataColumnRepository.saveAndFlush(new ExchangeDataDbTableColumn(colName, datatype));
            } else  {
                // representation exists
                representedColumns.remove(colName);
            }
        }

        // removing references that do not exist anymore
        removeRepresentation(representedColumns, exchangeDataColumnRepository);


        addColumn("eur", ColumnDatatype.DOUBLE);
        addColumn("usd", ColumnDatatype.DOUBLE);
        addColumn("gbp", ColumnDatatype.DOUBLE);
        addColumn("jpy", ColumnDatatype.DOUBLE);
        addColumn("cad", ColumnDatatype.DOUBLE);
        addColumn("cny", ColumnDatatype.DOUBLE);
    }

    public void removeColumn(String columnName) {
        Optional<ExchangeDataDbTableColumn> column = exchangeDataColumnRepository.findByName(columnName);
        if(column.isPresent()) {
            column.get().setElementSelections(null);
            removeAbstractColumn(column.get().getName(), TABLE_NAME, exchangeDataColumnRepository);
        }
    }

    @Override
    protected void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, exchangeDataColumnRepository, new ExchangeDataDbTableColumn(colName, datatype));
    }
}
