package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionDataDbManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionDataDbTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

@Transactional
@Service
@Lazy
public class DbTransactionManager {
    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    private TransactionDataColumnRepository transactionDataColumnRepository;
    @Autowired
    private StockDataDbManager stockDataDbManager;
    @Autowired
    private ImportTabManager importTabManager;
    @Autowired
    private CorrelationManager correlationManager;

    public HashMap<String, PreparedStatement> createStockDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for (StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            try {
                statements.put(column.getName(), stockDataDbManager.getPreparedStatement(column.getName(), connection));
            } catch (SQLException e) {
                e.printStackTrace();
                importTabManager.addToLog("ERR:\t\tErstellung des Statements fehlgeschlagen. Spalte: '"
                        + column.getName() + "' Datentyp '" + column.getColumnDatatype() + "' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
    }

    public HashMap<String, PreparedStatement> createTransactionDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for (TransactionDataDbTableColumn column : transactionDataColumnRepository.findAll()) {
            ColumnDatatype type = column.getColumnDatatype();
            String colName = column.getName();

            try {
                statements.put(colName, getPreparedTransactionStatement(colName, connection));
            } catch (SQLException e) {
                e.printStackTrace();
                importTabManager.addToLog("ERR:\t\tErstellung des Statements fehlgeschlagen. Spalte: '"
                        + colName + "' Datentyp '" + type + "' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
    }

    public PreparedStatement getPreparedTransactionStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO "+ TransactionDataDbManager.TABLE_NAME +" (depot_id, transaktions_datum, wertpapier_isin, " + dbColName + ") VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " + dbColName + "=VALUES(" + dbColName + ");";
        return connection.prepareStatement(sql);
    }

    public boolean executeStatements(Connection connection, HashMap<String, PreparedStatement> statements) {
        boolean silentError = false;
        try {
            for (PreparedStatement statement : statements.values()) {
                statement.executeBatch();
                statement.close();
            }
            connection.close();
        } catch (SQLException e) {
            silentError = true;
            importTabManager.addToLog("ERR:\t\t" + e.getMessage() + " _CAUSE_ " + e.getCause());
        }
        return silentError;
    }

    public boolean fillStockStatementAddToBatch(String isin, Date date, PreparedStatement statement,
                                                String data, ColumnDatatype datatype) {

        try {
            statement.setString(1, isin);
            statement.setDate(2, date);

            if (data == null) {
                fillNullByDataType(datatype, statement, 3);
            } else {
                fillByDataType(datatype, statement, 3, data);
            }

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Parsen des Wertes '" + data + "' in das Format "
                    + datatype.name() + " ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        }
        return true;
    }

    public boolean fillTransactionStatementAddToBatch(int depotId, Date date, String isin,
                                                      PreparedStatement statement, String data,
                                                      ColumnDatatype datatype) {

        try {
            statement.setInt(1, depotId);
            statement.setDate(2, date);
            statement.setString(3, isin);

            if (data == null) {
                fillNullByDataType(datatype, statement, 4);
            } else {
                fillByDataType(datatype, statement, 4, data);
            }

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Parsen des Wertes '" + data + "' in das Format "
                    + datatype.name() + " ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        }
        return true;
    }

    private void fillByDataType(ColumnDatatype datatype, PreparedStatement statement, int number, String data)
            throws SQLException, NumberFormatException, DateTimeParseException {

        switch (datatype) {
            case DATE -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dataToDate = LocalDate.from(formatter.parse(data));
                //LocalDate dataToDate = LocalDate.parse(data);
                statement.setDate(number, Date.valueOf(dataToDate));
            }
            case TEXT -> statement.setString(number, data);
            case INTEGER ->
                    // casting double to int to remove trailing zeros because of
                    // String.format("%.5f", cell.getNumericCellValue()).replace(",",".");
                    statement.setInt(number, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(number, Double.parseDouble(data));
            default -> {
            }
        }
    }

    private void fillNullByDataType(ColumnDatatype datatype, PreparedStatement statement, int index) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setNull(index, Types.DATE);
            case TEXT -> statement.setNull(index, Types.VARCHAR);
            case INTEGER -> statement.setInt(index, 0);
            case DOUBLE -> statement.setDouble(index, 0);
            /*  enable if handling for primitive data-types exists
                otherwise: null field --into-> primitive -> error
            case DATE -> statement.setNull(index, Types.DATE);
            case TEXT -> statement.setNull(index, Types.VARCHAR);
            case INTEGER -> statement.setNull(index, Types.INTEGER);
            case DOUBLE -> statement.setNull(index, Types.DOUBLE);
             */
            default -> {
            }
        }
    }
}