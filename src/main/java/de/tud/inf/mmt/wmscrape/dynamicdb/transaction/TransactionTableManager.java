package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

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
public class TransactionTableManager extends DbTableManger {

    public static final String TABLE_NAME = "depot_transaktion";
    public static final List<String> KEY_COLUMNS = List.of("depot_name", "transaktions_datum", "wertpapier_isin");
    public static final List<String> RESERVED_COLUMNS = List.of("depot_name", "transaktions_datum", "wertpapier_isin", "transaktionstyp");
    public static final List<String> COLUMN_ORDER = List.of("transaktions_datum", "depot_name", "wertpapier_isin", "transaktionstyp");

    @Autowired
    TransactionColumnRepository transactionColumnRepository;

    /**
     * <li> column-entities are managed based on the columns in the database</li>
     * <li> optional: predefined columns can be added to the db table with {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger#addColumn(String, VisualDatatype)}</li>
     */
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
    public void addColumn(String colName, VisualDatatype visualDatatype) {
        addColumnIfNotExists(TABLE_NAME, transactionColumnRepository, new TransactionColumn(colName, visualDatatype));
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

    public PreparedStatement getPreparedDataStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO `"+TABLE_NAME+"` (depot_name, transaktions_datum, wertpapier_isin, `"+dbColName+"`) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE `"+dbColName+"`=VALUES(`"+dbColName+"`);";
        return connection.prepareStatement(sql);
    }
}
