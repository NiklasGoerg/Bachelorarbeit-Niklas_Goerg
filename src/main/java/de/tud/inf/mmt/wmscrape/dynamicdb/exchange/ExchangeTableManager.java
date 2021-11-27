package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ExchangeTableManager extends DbTableManger {

    public static final String TABLE_NAME = "wechselkurse";
    public static final List<String> RESERVED_COLUMNS = List.of("datum");
    public static final List<String> COLUMN_ORDER = List.of("datum");

    @Autowired
    ExchangeColumnRepository exchangeColumnRepository;

    @PostConstruct
    private void initExchangeData() {
        // the exchange data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (datum DATE PRIMARY KEY);");
        }

        initTableColumns(exchangeColumnRepository, TABLE_NAME);

        addColumn("eur", ColumnDatatype.DOUBLE);
        addColumn("usd", ColumnDatatype.DOUBLE);
        addColumn("gbp", ColumnDatatype.DOUBLE);
        addColumn("jpy", ColumnDatatype.DOUBLE);
        addColumn("cad", ColumnDatatype.DOUBLE);
        addColumn("cny", ColumnDatatype.DOUBLE);
    }


    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, exchangeColumnRepository);
    }

    @Override
    public void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, exchangeColumnRepository, new ExchangeColumn(colName, datatype));
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
        exchangeColumnRepository.saveAndFlush(new ExchangeColumn(colName, datatype));
    }
}
