package de.tud.inf.mmt.wmscrape.gui.tabs.dbData.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.CustomCell;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.StockRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class StockAndCourseTabManager {

    @Autowired
    private StockDataColumnRepository columnRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private DataSource dataSource;

    public void prepareStockSelectionTable(TableView<Stock> table) {
        TableColumn<Stock, String> nameCol =  new TableColumn<>("Name");
        TableColumn<Stock, String> isinCol =  new TableColumn<>("ISIN");
        TableColumn<Stock, String> wknCol =  new TableColumn<>("WKN");
        TableColumn<Stock, String> typCol =  new TableColumn<>("typ");

        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        isinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsin()));
        wknCol.setCellValueFactory(param -> param.getValue().wknProperty());
        typCol.setCellValueFactory(param -> param.getValue().stockTypeProperty());

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        wknCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typCol.setCellFactory(TextFieldTableCell.forTableColumn());

        nameCol.setEditable(true);
        isinCol.setEditable(false);
        wknCol.setEditable(true);
        typCol.setEditable(true);

        table.getColumns().add(isinCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(wknCol);
        table.getColumns().add(typCol);
    }

    public void updateStockSelectionTable(TableView<Stock> table) {
        table.getItems().addAll(stockRepository.findAll());
    }


    public ObservableList<CustomRow> updateStockTable(TableView<CustomRow> table) {
        List<? extends DbTableColumn> dbTableColumns = getTableColumns(columnRepository);
        prepareTable(table, dbTableColumns, StockDataDbManager.RESERVED_COLUMNS, StockDataDbManager.COLUMN_ORDER);
        return getAllRows(StockDataDbManager.TABLE_NAME,dbTableColumns);
    }

    private  <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns( DynamicDbRepository<T, Integer> repository) {
        return repository.findAll();
    }

    private <T extends DbTableColumn> void prepareTable(TableView<CustomRow> table,
                                                       List<? extends DbTableColumn> columns,
                                                       List<String> reserved, List<String> order) {

        for(DbTableColumn dbColumn : columns) {
            String colName = dbColumn.getName();
            ColumnDatatype datatype = dbColumn.getColumnDatatype();

            TableColumn<CustomRow, String> tableColumn = new TableColumn<>(colName);

            // binding the custom cell property directly to the table cell
            tableColumn.setCellValueFactory(param -> param.getValue().getCells().get(colName).textDataProperty());
            tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            setComparator(tableColumn, datatype);
            tableColumn.setPrefWidth(150);

            if(reserved.contains(colName)) tableColumn.setEditable(false);

            // moving important to front
            if(order.contains(colName) && (table.getColumns().size() >= order.indexOf(colName))) {
                table.getColumns().add(order.indexOf(colName), tableColumn);
            } else table.getColumns().add(tableColumn);
        }

    }

    private void setComparator(TableColumn<CustomRow, String> column, ColumnDatatype datatype){

        if(datatype == ColumnDatatype.TEXT) return;

        column.setComparator((x, y) -> {

            if (x == null && y == null) return 0;
            if (x == null) return -1;
            if (y == null) return 1;

            switch (datatype) {
                case INTEGER -> {return Integer.valueOf(x).compareTo(Integer.valueOf(y));}
                case DOUBLE -> {return Double.valueOf(x).compareTo(Double.valueOf(y));}
                case DATE -> {return Date.valueOf(x).compareTo(Date.valueOf(y));}
            }

            return 0;
        });
    }

    public ObservableList<CustomRow> getAllRows(String tableName,  List<? extends DbTableColumn> columns) {

        ObservableList<CustomRow> allRows = FXCollections.observableArrayList();

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM "+tableName);

            // for each db row create new custom row
            while (results.next()) {
                CustomRow row =  new CustomRow();
                // for each column create new custom column
                for(DbTableColumn column : columns) {
                    CustomCell cell = new CustomCell(column, results.getString(column.getName()));
                    row.addCell(column.getName(),cell);
                }
                allRows.add(row);
            }

            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allRows;
    }

    public ObservableList<CustomRow> getRowsFromStockSelection(Stock stock, ObservableList<CustomRow> rows) {
        String stockIsin = stock.getIsin();

        ObservableList<CustomRow> objects = FXCollections.observableArrayList();
        for (CustomRow row : rows) {
            if(row.getCells().containsKey("isin")) {
                if (row.getCells().get("isin").textDataProperty().get().equals(stockIsin)) {
                    objects.add(row);
                }
            }
        }
        return objects;
    }


    // todo alert save success
    public void saveChangedRows(List<CustomRow> rows) {
        if(rows == null || rows.size() == 0) return;
        Connection connection = null;
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        try {
            connection = dataSource.getConnection();

            for(CustomRow row : rows) {
                String isin = row.getCells().get("isin").getTextData();
                String date = row.getCells().get("datum").getTextData();

                for(CustomCell cell : row.getChangedCells()) {

                    PreparedStatement stmt;
                    if(!statements.containsKey(cell.getColumnName())) {
                        stmt = prepareStatements(cell.getColumnName(), connection);
                        statements.put(cell.getColumnName(), stmt);
                    } else {
                        stmt = statements.get(cell.getColumnName());
                    }

                    fillByDataType( stmt, cell.getTextData(), cell.getDatatype(), 1);
                    fillByDataType(stmt, date, ColumnDatatype.DATE, 2);
                    fillByDataType(stmt, isin, ColumnDatatype.TEXT, 3);

                    stmt.addBatch();
                }
            }

            for (PreparedStatement s : statements.values()) {
                s.executeBatch();
                s.close();
            }
            connection.close();

        } catch (SQLException | NumberFormatException | DateTimeParseException e) {
            // todo alert save error
            e.printStackTrace();
            closeConnection(connection);
        }
    }

    public void saveStockListChanges(ObservableList<Stock> stocks) {
        stockRepository.saveAllAndFlush(stocks);
    }

    private void closeConnection(Connection connection) {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private PreparedStatement prepareStatements(String colName, Connection connection) throws SQLException{
        String sql = "INSERT INTO "+StockDataDbManager.TABLE_NAME+" ("+colName+
                ", datum, isin) VALUES(?,?,?) ON DUPLICATE KEY UPDATE "+colName+"=VALUES("+colName+");";
        return connection.prepareStatement(sql);
    }


    protected void fillByDataType(PreparedStatement statement, String data, ColumnDatatype datatype, int index)
            throws SQLException,NumberFormatException, DateTimeParseException {

        if (data == null || data.isBlank()) {
            fillNullByDataType(index, datatype, statement);
            return;
        }

        switch (datatype) {
            case DATE -> {
                LocalDate dataToDate = LocalDate.from(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(data));
                statement.setDate(index, Date.valueOf(dataToDate));
            }
            case TEXT -> statement.setString(index, data);
            case INTEGER -> statement.setInt(index, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(index, Double.parseDouble(data));
        }
    }

    protected void fillNullByDataType(int index, ColumnDatatype datatype, PreparedStatement statement) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setNull(index, Types.DATE);
            case TEXT -> statement.setNull(index, Types.VARCHAR);
            case INTEGER -> statement.setInt(index, 0);
            case DOUBLE -> statement.setDouble(index, 0);
        }
    }

    // todo error alert
    public void deleteRows(List<CustomRow> rows, boolean everything) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            if(everything) deleteEverything(connection, rows);
            else deleteSelection(connection, rows);

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            closeConnection(connection);
        }
    }

    private void deleteEverything(Connection connection, List<CustomRow> rows) throws SQLException {
        PreparedStatement statement = prepareDeleteAllStatement(connection);

        for(CustomRow row : rows) {
            fillDeleteAllStatement(row, statement);
            statement.addBatch();
        }

        statement.executeBatch();
        statement.close();
    }

    private void deleteSelection(Connection connection, List<CustomRow> rows) throws SQLException {
        PreparedStatement statement = prepareDeleteSelectionStatement(connection);

        for(CustomRow row : rows) {
            fillDeleteSelectionStatement(row, statement);
            statement.addBatch();
        }

        statement.executeBatch();
        statement.close();
    }

    // todo abstract
    private void fillDeleteAllStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("isin") == null) return;

        String isin = cells.get("isin").getTextData();
        fillByDataType(statement, isin, ColumnDatatype.TEXT, 1);
    }


    // todo abstract
    private void fillDeleteSelectionStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("isin") == null || cells.get("datum") == null) return;

        String isin = cells.get("isin").getTextData();
        String datum = cells.get("datum").getTextData();

        fillByDataType(statement, isin, ColumnDatatype.TEXT, 1);
        fillByDataType(statement, datum, ColumnDatatype.DATE, 2);
        statement.addBatch();
    }

    // todo abstract
    private PreparedStatement prepareDeleteSelectionStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM "+StockDataDbManager.TABLE_NAME+" WHERE isin=? and datum=?");
    }

    private PreparedStatement prepareDeleteAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM "+StockDataDbManager.TABLE_NAME+" WHERE isin=?");
    }

}
