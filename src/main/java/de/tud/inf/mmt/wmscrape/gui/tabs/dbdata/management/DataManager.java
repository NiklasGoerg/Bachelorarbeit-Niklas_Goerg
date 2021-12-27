package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomCell;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataManager {


    @Autowired protected DataSource dataSource;
    @Autowired protected StockRepository stockRepository;
    @Autowired private DepotRepository depotRepository;
    @Autowired private StockColumnRepository stockColumnRepository;

    protected DbTableColumnRepository<? extends DbTableColumn, Integer> dbTableColumnRepository;
    protected DbTableManger dbTableManger;


    @PostConstruct
    protected abstract void setColumnRepositoryAndManager();

    protected abstract Map<String, String> getKeyInformation(CustomRow row);

    protected abstract void setStatementKeys(CustomCell cell, PreparedStatement stmt ,
                                             Map<String, String> keys) throws SQLException;

    protected abstract PreparedStatement prepareUpdateStatements(String colName, Connection connection) throws SQLException;

    protected abstract PreparedStatement prepareDeleteAllStatement(Connection connection) throws SQLException ;

    protected abstract PreparedStatement prepareDeleteSelectionStatement(Connection connection) throws SQLException ;

    protected abstract void fillDeleteAllStatement(CustomRow row, PreparedStatement statement) throws SQLException ;

    protected abstract void fillDeleteSelectionStatement(CustomRow row, PreparedStatement statement) throws SQLException ;


    protected <T extends DbTableColumn> void prepareTable(TableView<CustomRow> table,
                                                          List<T> columns,
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
            case INTEGER -> statement.setNull(index, Types.INTEGER);
            case DOUBLE -> statement.setNull(index, Types.DOUBLE);
        }
    }

    public boolean deleteRows(List<CustomRow> rows, boolean everything) {
        if(rows == null) return true;

        try (Connection connection = dataSource.getConnection()) {

            if(everything) deleteEverything(connection, rows);
            else deleteSelection(connection, rows);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    public ObservableList<CustomRow> updateDataTable(TableView<CustomRow> table) {
        List<? extends DbTableColumn> dbTableColumns = getTableColumns(dbTableColumnRepository);
        prepareTable(table, dbTableColumns, dbTableManger.getKeyColumns(), dbTableManger.getColumnOrder());
        return getAllRows(dbTableManger.getTableName(), dbTableColumns);
    }

    public ObservableList<CustomRow> getRowsBySelection(String key, String keyValue, ObservableList<CustomRow> rows) {

        ObservableList<CustomRow> objects = FXCollections.observableArrayList();
        for (CustomRow row : rows) {
            if(row.getCells().containsKey(key)) {
                if (row.getCells().get(key).textDataProperty().get().equals(keyValue)) {
                    objects.add(row);
                }
            }
        }
        return objects;
    }

    public ObservableList<CustomRow> getAllRows(String tableName,  List<? extends DbTableColumn> columns) {

        ObservableList<CustomRow> allRows = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allRows;
    }

    private  <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns( DbTableColumnRepository<T, Integer> repository) {
        return repository.findAll();
    }

    public boolean saveChangedRows(List<CustomRow> rows) {
        if(rows == null || rows.size() == 0) return true;
        HashMap<String, PreparedStatement> statements = new HashMap<>();

        try (Connection connection = dataSource.getConnection()){

            for(CustomRow row : rows) {
                var keys = getKeyInformation(row);
                if (keys == null) return false;

                for(CustomCell cell : row.getChangedCells()) {

                    PreparedStatement stmt;
                    if(!statements.containsKey(cell.getColumnName())) {
                        stmt = prepareUpdateStatements(cell.getColumnName(), connection);
                        statements.put(cell.getColumnName(), stmt);
                    } else {
                        stmt = statements.get(cell.getColumnName());
                    }

                    setStatementKeys(cell, stmt, keys);
                    fillByDataType( stmt, cell.getTextData(), cell.getDatatype(), 1);
                    stmt.addBatch();
                }
            }

            for (PreparedStatement s : statements.values()) {
                s.executeBatch();
                s.close();
            }

        } catch (SQLException | NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void prepareStockSelectionTable(TableView<Stock> table) {
        TableColumn<Stock, String> nameCol =  new TableColumn<>("Name");
        TableColumn<Stock, String> isinCol =  new TableColumn<>("ISIN");
        TableColumn<Stock, String> wknCol =  new TableColumn<>("WKN");
        TableColumn<Stock, String> typCol =  new TableColumn<>("Typ");

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

    public void prepareDepotSelectionTable(TableView<Depot> table) {
        TableColumn<Depot, String> nameCol =  new TableColumn<>("Name");
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        nameCol.setEditable(false);
        table.getColumns().add(nameCol);
    }

    public void updateDepotSelectionTable(TableView<Depot> table) {
        table.getItems().addAll(depotRepository.findAll());
    }


    public void saveStockListChanges(ObservableList<Stock> stocks) {
        stockRepository.saveAllAndFlush(stocks);
    }

    public void deleteStock(Stock stock) {
        stockRepository.delete(stock);
    }

    public boolean createStock(String isin, String wkn, String name, String type) {
        if(isin == null || isin.isBlank()) return false;

        if(stockRepository.findByIsin(isin).isPresent()) return false;
        stockRepository.saveAndFlush(new Stock(isin, wkn, name, type));
        return true;
    }

    public List<? extends DbTableColumn>  getDbTableColumns() {
        var all = dbTableColumnRepository.findAll();
        all.removeIf(column -> dbTableManger.getReservedColumns().contains(column.getName()));
        return all;
    }

    public void addColumn(String colName, ColumnDatatype datatype) {
        dbTableManger.addColumn(colName, datatype);
    }

    public boolean removeColumn(String colName) {
        return dbTableManger.removeColumn(colName);
    }
}
