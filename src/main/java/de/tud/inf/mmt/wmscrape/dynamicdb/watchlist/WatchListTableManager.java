package de.tud.inf.mmt.wmscrape.dynamicdb.watchlist;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class WatchListTableManager extends DbTableManger {

    public static final String TABLE_NAME = "watch_list";
    public static final List<String> NOT_EDITABLE_COLUMNS = List.of("isin", "datum");
    public static final List<String> RESERVED_COLUMNS = List.of("isin", "datum");
    public static final List<String> COLUMN_ORDER = List.of("isin", "datum");

    @Autowired
    WatchListColumnRepository watchListColumnRepository;

    /**
     * <li> creates the table in the database because it is not managed by hibernate.</li>
     * <li> a constraint between the "wertpapier" table and the "watch_list" is added</li>
     * <li> column-entities are managed based on the columns in the database</li>
     * <li> optional: predefined columns can be added to the db table with the {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger}</li>
     */
    @PostConstruct
    private void initWatchListData() {
        // the stock data table is not managed by spring
        // and has to be initialized by WMScrape

        if (tableDoesNotExist(TABLE_NAME)) {
            executeStatement("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");

            executeStatement("CREATE UNIQUE INDEX i_isin_wl ON "+TABLE_NAME+" (isin)");

            // set the foreign keys to the newly created table
            // do not constrain non hibernate tables. the creation oder is not known for them (if not set -> @Order)
            executeStatement("ALTER TABLE "+TABLE_NAME+
                    // constraint name doesn't matter. watch_list_isin
                    " ADD CONSTRAINT fk_watch_list_isin FOREIGN KEY (isin)"+
                    // watch_list = table name of stock entity
                    " REFERENCES "+TABLE_NAME+" (isin)"+" ON DELETE CASCADE"+" ON UPDATE CASCADE");
        }

        initTableColumns(watchListColumnRepository, TABLE_NAME);
    }

    /**
     * removes a column from table watch_list
     * @param columnName name of column to remove
     * @return boolean result if removal was successful
     */
    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, watchListColumnRepository);
    }

    /**
     * @param colName the name of the column to be added
     * @param visualDatatype the datatype that will be used to represent the value of the column will be translated into a {@link ColumnDatatype}
     */
    @Override
    public void addColumn(String colName, VisualDatatype visualDatatype) {
        addColumnIfNotExists(TABLE_NAME, watchListColumnRepository, new WatchListColumn(colName, visualDatatype));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getNotEditableColumns() {
        return NOT_EDITABLE_COLUMNS;
    }

    @Override
    public List<String> getReservedColumns() {
        return RESERVED_COLUMNS;
    }

    @Override
    public List<String> getDefaultColumnOrder() { return COLUMN_ORDER; }

    @Override
    protected void saveNewInRepository(String colName, ColumnDatatype datatype) {
        watchListColumnRepository.saveAndFlush(new WatchListColumn(colName, datatype));
    }

    /**
     *
     * @param colName the name of the column
     * @param connection a sql-connection
     * @return a prepared statement to insert values into the tabel
     * @throws SQLException in case of a database error
     */
    @Override
    public PreparedStatement getPreparedDataStatement(String colName, Connection connection) throws SQLException {
        String sql = "INSERT INTO `"+TABLE_NAME+"` (isin, datum, `"+colName+"`) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `"+
                colName+"`=VALUES(`"+colName+"`);";
        return connection.prepareStatement(sql);

    }
}
