package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumn;
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
    private StockColumnRepository stockColumnRepository;
    @Autowired
    private TransactionColumnRepository transactionColumnRepository;
    @Autowired
    private StockTableManager stockDataDbManager;
    @Autowired
    private ImportTabManager importTabManager;
    @Autowired
    private CorrelationManager correlationManager;

    public HashMap<String, PreparedStatement> createStockDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for (StockColumn column : stockColumnRepository.findAll()) {
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

        for (TransactionColumn column : transactionColumnRepository.findAll()) {
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
        String sql = "INSERT INTO `"+ TransactionTableManager.TABLE_NAME +"` (depot_name, transaktions_datum, wertpapier_isin, `" + dbColName + "`) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE `" + dbColName + "`=VALUES(`" + dbColName + "`);";
        return connection.prepareStatement(sql);
    }

    public boolean executeStatements(Connection connection, HashMap<String, PreparedStatement> statements) {
        try {
            for (PreparedStatement statement : statements.values()) {
                statement.executeBatch();
                statement.close();
            }
            connection.close();
        } catch (SQLException e) {
            importTabManager.addToLog("ERR:\t\t" + e.getMessage() + " _CAUSE_ " + e.getCause());
            return true;
        }
        return false;
    }

    public boolean fillStockStatementAddToBatch(String isin, Date date, PreparedStatement statement,
                                                String data, ColumnDatatype datatype) {

        try {
            statement.setString(1, isin);
            statement.setDate(2, date);

            if (data == null) {
                fillNullByDataType(datatype, statement, 3, true);
            } else {
                fillByDataType(datatype, statement, 3, data);
            }

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return true;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Parsen des Wertes '" + data + "' in das Format "
                    + datatype.name() + " ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return true;
        }
        return false;
    }

    public boolean fillTransactionStatementAddToBatch(String depotName, Date date, String isin,
                                                      PreparedStatement statement, String data,
                                                      ColumnDatatype datatype) {

        try {
            statement.setString(1, depotName);
            statement.setDate(2, date);
            statement.setString(3, isin);

            if (data == null) {
                fillNullByDataType(datatype, statement, 4, false);
            } else {
                fillByDataType(datatype, statement, 4, data);
            }

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return true;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            importTabManager.addToLog("ERR:\t\tBei dem Parsen des Wertes '" + data + "' in das Format "
                    + datatype.name() + " ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return true;
        }
        return false;
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
                    // String.format("%.6f", cell.getNumericCellValue()).replace(",",".");
                    statement.setInt(number, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(number, Double.parseDouble(data));
            default -> {
            }
        }
    }

    private void fillNullByDataType(ColumnDatatype datatype, PreparedStatement statement, int index, boolean physicalNull)
            throws SQLException {

        // setting number values to 0 instead of null because otherwise I would have to use
        // Integer inside the Transaction Object to allow Null values

        switch (datatype) {
            case DATE -> statement.setNull(index, Types.DATE);
            case TEXT -> statement.setNull(index, Types.VARCHAR);
            case INTEGER -> {
                if(physicalNull) statement.setNull(index, Types.INTEGER);
                else statement.setInt(index, 0);
            }
            case DOUBLE -> {
                if(physicalNull) statement.setNull(index, Types.DOUBLE);
                else statement.setDouble(index, 0);
            }
            default -> {
            }
        }
    }
}