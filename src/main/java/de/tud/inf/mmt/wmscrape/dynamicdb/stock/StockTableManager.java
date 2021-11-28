package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class StockTableManager extends DbTableManger {

    public static final String TABLE_NAME = "stammdaten";
    public static final List<String> KEY_COLUMNS = List.of("datum","isin");
    public static final List<String> RESERVED_COLUMNS = List.of("datum", "isin", "wkn", "name", "typ");
    public static final List<String> COLUMN_ORDER = List.of("datum","isin","wkn","name","typ");

    @Autowired
    StockColumnRepository stockColumnRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    TransactionTemplate transactionTemplate;

    @PostConstruct
    private void initStockData() {
        // the stock data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        initTableColumns(stockColumnRepository, TABLE_NAME);

        // todo remove
        addColumn("url_1", ColumnDatatype.TEXT);
        addColumn("url_2", ColumnDatatype.TEXT);
        addColumn("kurs", ColumnDatatype.DOUBLE);
        addColumn("gruppe", ColumnDatatype.INTEGER);
        addColumn("Std_Datum", ColumnDatatype.DATE);
    }

    public PreparedStatement getPreparedStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO "+TABLE_NAME+" (isin, datum, " + dbColName + ") VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";
        return connection.prepareStatement(sql);
    }

    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, stockColumnRepository);
    }

    @Override
    public void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, stockColumnRepository, new StockColumn(colName, datatype));
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
    public List<String> getReserverdColumns() {
        return RESERVED_COLUMNS;
    }

    @Override
    public List<String> getColumnOrder() {
        return COLUMN_ORDER;
    }

    @Override
    protected void saveNewInRepository(String colName, ColumnDatatype datatype) {
        stockColumnRepository.saveAndFlush(new StockColumn(colName, datatype));
    }
}
