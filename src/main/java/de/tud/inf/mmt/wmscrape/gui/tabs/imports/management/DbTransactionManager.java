package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

@Transactional
@Service
public class DbTransactionManager {
    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    private ImportTabManager importTabManager;
    @Autowired
    private CorrelationManager correlationManager;

    HashMap<String, ColumnDatatype> getStockColDbDatatypes() {
        HashMap<String, ColumnDatatype> columnDatatypes = new HashMap<>();
        for (StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            columnDatatypes.put(column.getName(), column.getColumnDatatype());
        }
        return columnDatatypes;
    }

    HashMap<String, PreparedStatement> createStockDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for (StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            try {
                statements.put(column.getName(), getPreparedStockStatement(column.getName(), connection));
            } catch (SQLException e) {
                e.printStackTrace();
                importTabManager.addToLog("FEHLER: Erstellung des Statements fehlgeschlagen. Spalte: '"
                        + column.getName() + "' Datentyp '" + column.getColumnDatatype() + "' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
    }

    HashMap<String, PreparedStatement> createTransactionDataStatements(Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        var transactionColumnsWithType = correlationManager.getTransactionColumnsWithType();
        for (String colName : transactionColumnsWithType.keySet()) {
            ColumnDatatype type = transactionColumnsWithType.get(colName);
            try {
                statements.put(colName, getPreparedTransactionStatement(colName, connection));
            } catch (SQLException e) {
                e.printStackTrace();
                importTabManager.addToLog("FEHLER: Erstellung des Statements fehlgeschlagen. Spalte: '"
                        + colName + "' Datentyp '" + type + "' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
    }

    public PreparedStatement getPreparedTransactionStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO depottransaktion (depot_id, zeitpunkt, wertpapier_isin, " + dbColName + ") VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " + dbColName + "=VALUES(" + dbColName + ");";
        return connection.prepareCall(sql);
    }

    public PreparedStatement getPreparedStockStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO stammdaten (isin, datum, " + dbColName + ") VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";
        return connection.prepareCall(sql);
    }

    boolean executeStatements(Connection connection, HashMap<String, PreparedStatement> statements) {
        boolean silentError = false;
        try {
            for (PreparedStatement statement : statements.values()) {
                statement.executeBatch();
                statement.close();
            }
            connection.close();
        } catch (SQLException e) {
            silentError = true;
            importTabManager.addToLog("FEHLER: " + e.getMessage() + " _CAUSE_ " + e.getCause());
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
            importTabManager.addToLog("FEHLER: Bei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            importTabManager.addToLog("FEHLER: Bei dem Parsen des Wertes '" + data + "' in das Format "
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
            importTabManager.addToLog("FEHLER: Bei dem Setzen der Statementwerte sind Fehler aufgetreten: "
                    + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            importTabManager.addToLog("FEHLER: Bei dem Parsen des Wertes '" + data + "' in das Format "
                    + datatype.name() + " ist ein Fehler aufgetreten. " + e.getMessage() + " _ CAUSE_ " + e.getCause());
            return false;
        }
        return true;
    }

    public void fillByDataType(ColumnDatatype datatype, PreparedStatement statement, int number, String data) throws SQLException {
        switch (datatype) {
            case DATE -> {
                LocalDate dataToDate = LocalDate.parse(data);
                statement.setDate(number, Date.valueOf(dataToDate));
            }
            case TEXT -> statement.setString(number, data);
            case INT ->
                    // casting double to int to remove trailing zeros because of
                    // String.format("%.5f", cell.getNumericCellValue()).replace(",",".");
                    statement.setInt(number, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(number, Double.parseDouble(data));
            default -> {
            }
        }
    }

    public void fillNullByDataType(ColumnDatatype datatype, PreparedStatement statement, int number) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setDate(number, null);
            case TEXT -> statement.setString(number, null);
            case INT -> statement.setInt(number, 0);
            case DOUBLE -> statement.setDouble(number, 0);
            default -> {
            }
        }
    }
}