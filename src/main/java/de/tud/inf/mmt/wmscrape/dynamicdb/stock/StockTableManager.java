package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
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

    public static final String TABLE_NAME = "wertpapier_stammdaten";
    public static final List<String> KEY_COLUMNS = List.of("datum","isin");
    public static final List<String> RESERVED_COLUMNS = List.of("datum", "isin", "wkn", "name", "typ");
    public static final List<String> COLUMN_ORDER = List.of("datum","isin","wkn","name","typ");

    @Autowired
    StockColumnRepository stockColumnRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    TransactionTemplate transactionTemplate;

    /**
     * <li> creates the table in the database because it is not managed by hibernate.</li>
     * <li> a constraint between the "wertpapier" table and the "wertpapier_stammdaten" is added</li>
     * <li> column-entities are managed based on the columns in the database</li>
     * <li> optional: predefined columns can be added to the db table with the {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger}</li>
     */
    @PostConstruct
    private void initStockData() {
        // the stock data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            executeStatement("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");

            // set the foreign keys to the newly created table
            // do not constraint non hibernate tables. the creation oder is not known for them (if not set -> @Order)
            executeStatement("ALTER TABLE "+TABLE_NAME+
                    // constraint name doesn't matter. wertpapier_stammdaten.isin
                    " ADD CONSTRAINT fk_wertpapier_isin_stammdaten FOREIGN KEY (isin)"+
                    // wertpapier = table name of stock entity
                    " REFERENCES wertpapier (isin)"+" ON DELETE CASCADE"+" ON UPDATE CASCADE");
        }

        initTableColumns(stockColumnRepository, TABLE_NAME);

        //addColumn("url_1", ColumnDatatype.TEXT);
    }

    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, stockColumnRepository);
    }

    @Override
    public void addColumn(String colName, VisualDatatype visualDatatype) {
        addColumnIfNotExists(TABLE_NAME, stockColumnRepository, new StockColumn(colName, visualDatatype));
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
        stockColumnRepository.saveAndFlush(new StockColumn(colName, datatype));
    }

    @Override
    public PreparedStatement getPreparedDataStatement(String colName, Connection connection) throws SQLException {
        String sql = "INSERT INTO "+TABLE_NAME+" (isin, datum, `"+colName+"`) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `"+
                colName+"`=VALUES(`"+colName+"`);";
        return connection.prepareStatement(sql);

    }
}
