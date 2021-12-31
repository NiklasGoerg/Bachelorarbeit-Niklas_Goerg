package de.tud.inf.mmt.wmscrape.gui.tabs.imports.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private ImportTabManager importTabManager;
    @Autowired
    private CorrelationManager correlationManager;

    String dateToday;

    public DbTransactionManager() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateToday = dateFormat.format(Date.valueOf(LocalDate.now()));
    }

    public <T extends DbTableColumnRepository<? extends DbTableColumn, Integer>> HashMap<String, PreparedStatement>
                                            createDataStatements(DbTableManger tableManger, T repository, Connection connection) {
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        // prepare a statement for each column

        for (DbTableColumn column : repository.findAll()) {
            String colName = column.getName();

            try {
                statements.put(colName, tableManger.getPreparedDataStatement(colName, connection));
            } catch (SQLException e) {
                e.printStackTrace();
                importTabManager.addToLog("ERR:\t\tErstellung des Statements fehlgeschlagen. Spalte: '"
                        + colName + "' Datentyp '" + column.getColumnDatatype() + "' _CAUSE_ " + e.getCause());
                return null;
            }
        }
        return statements;
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

    public boolean fillStockStatementAddToBatch(String isin, PreparedStatement statement,
                                                String data, ColumnDatatype datatype) {

        if (setStatementValue(1, isin, ColumnDatatype.TEXT, statement)) return true;
        if (setStatementValue(2, dateToday, ColumnDatatype.DATE, statement)) return true;
        if (setStatementValue(3, data, datatype, statement)) return true;
        return addBatch(statement);
    }

    public boolean fillTransactionStatementAddToBatch(String depotName, String isin, String date,
                                                      PreparedStatement statement, String data,
                                                      ColumnDatatype datatype) {

        if (setStatementValue(1, depotName, ColumnDatatype.TEXT, statement)) return true;
        if (setStatementValue(2, date, ColumnDatatype.DATE, statement)) return true;
        if (setStatementValue(3, isin, ColumnDatatype.TEXT, statement)) return true;
        if (setStatementValue(4, data, datatype, statement)) return true;
        return addBatch(statement);
    }


    private boolean setStatementValue(int i, String data, ColumnDatatype datatype, PreparedStatement statement) {
        try {
            if (data == null) {
                fillNullByDataType(datatype, statement, i, false);
            } else {
                fillByDataType(datatype, statement, i, data);
            }
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

    private boolean addBatch(PreparedStatement statement) {
        try {
            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
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