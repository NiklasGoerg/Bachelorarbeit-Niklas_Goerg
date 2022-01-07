package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.*;
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

    public abstract boolean addRowForSelection(Object selection);

    /**
     * used to set the correct column repository and manger after bean creation
     */
    @PostConstruct
    protected abstract void setColumnRepositoryAndManager();

    /**
     * extracts the primary key information from one row
     *
     * @param row the row from which the keys should be extracted
     * @return the primary keys
     */
    protected abstract Map<String, String> getKeyInformation(CustomRow row);

    /**
     * sets the previously extracted keys into a statement
     *
     * @param stmt the prepared statement to fill
     * @param keys the extracted primary key values
     */
    protected abstract void setStatementKeys(PreparedStatement stmt ,
                                             Map<String, String> keys) throws SQLException;

    /**
     * creates a statement to persist changed rows
     *
     * @param colName the column name where the data will be persisted
     * @param connection jdbc connection
     * @return the statement containing the key information
     */
    protected abstract PreparedStatement prepareUpdateStatements(String colName, Connection connection) throws SQLException;

    /**
     * creates a statement to delete all rows in a table
     *
     * @param connection jdbc connection
     * @return the statement ready for data insertion
     */
    protected abstract PreparedStatement prepareDeleteAllStatement(Connection connection) throws SQLException ;

    /**
     * creates a statement used for deleteting single rows
     *
     * @param connection jdbc connection
     * @return the statement ready for data insertion
     */
    protected abstract PreparedStatement prepareDeleteSelectionStatement(Connection connection) throws SQLException ;

    /**
     * sets the actual data into the prepared statements given the row
     *
     * @param row the row to be deleted
     * @param statement previously prepared statement ready to set the data
     */
    protected abstract void fillDeleteAllStatement(CustomRow row, PreparedStatement statement) throws SQLException ;

    /**
     * sets the actual data into the prepared statements given the row
     *
     * @param row the row to be deleted
     * @param statement previously prepared statement ready to set the data
     */
    protected abstract void fillDeleteSelectionStatement(CustomRow row, PreparedStatement statement) throws SQLException ;

    /**
     * prepares the data table to represent the custom rows and columns
     *
     * @param table the javafx table
     * @param columns all columns as a list of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn} subclass Objects
     * @param reserved the columns that are not supposed to be editable
     * @param order the arrangement order of some columns
     * @param <T> subclass of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn}
     */
    protected <T extends DbTableColumn> void prepareTable(TableView<CustomRow> table,
                                                          List<T> columns,
                                                          List<String> reserved, List<String> order) {

        for(DbTableColumn dbColumn : columns) {
            String colName = dbColumn.getName();
            ColumnDatatype datatype = dbColumn.getColumnDatatype();

            TableColumn<CustomRow, String> tableColumn = new TableColumn<>(colName);

            // binding the event handlers to the cell
            tableColumn.addEventHandler(TableColumn.editStartEvent(), event ->
                    table.getSelectionModel().getSelectedItem().getCells().get(colName).onEditStartEvent());
            tableColumn.addEventHandler(TableColumn.editCancelEvent(), event ->
                    table.getSelectionModel().getSelectedItem().getCells().get(colName).onEditCancelEvent());
            tableColumn.addEventHandler(TableColumn.editCommitEvent(), event ->
                    table.getSelectionModel().getSelectedItem().getCells().get(colName).onEditCommitEvent(event));

            // binding the custom cell property directly to the table cell
            tableColumn.setCellValueFactory(param -> param.getValue().getCells().get(colName).visualizedDataPropertyProperty());
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

    /**
     * adds sorting columns based on the datatype
     *
     * @param column the column to be sorted
     * @param datatype the column datatype
     */
    private void setComparator(TableColumn<CustomRow, String> column, ColumnDatatype datatype){

        if(datatype == ColumnDatatype.TEXT) return;

        column.setComparator((x, y) -> {

            if (x == null && y == null) return 0;
            if (x == null) return -1;
            if (y == null) return 1;

            try {
                switch (datatype) {
                    case INTEGER -> {return Integer.valueOf(cleanNumber(x)).compareTo(Integer.valueOf(cleanNumber(y)));}
                    case DOUBLE -> {return Double.valueOf(cleanNumber(x)).compareTo(Double.valueOf(cleanNumber(y)));}
                    case DATE -> {return Date.valueOf(x).compareTo(Date.valueOf(y));}
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }

            return 0;
        });
    }

    /**
     * had to be added because now there are symbols like €, $ or % that are directly inside the cell
     * and have to be filtered out before comparing.
     *
     * @param string the numerical value as string
     * @return a numerical value that can be parsed
     */
    private String cleanNumber(String string) {
        return string.replaceAll("[^+\\-0-9.]","");
    }

    /**
     *
     * @param statement the statement where the data will be set
     * @param data the data as string
     * @param datatype the data datatype
     * @param index the position where the data will be inserted into the given statement
     */
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

    /**
     * sets a null value into a statement based on the datatype
     *
     * @param index the position where the data will be inserted into the given statement
     * @param datatype the data datatype
     * @param statement the statement where the data will be set
     */
    protected void fillNullByDataType(int index, ColumnDatatype datatype, PreparedStatement statement) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setNull(index, Types.DATE);
            case TEXT -> statement.setNull(index, Types.VARCHAR);
            case INTEGER -> statement.setNull(index, Types.INTEGER);
            case DOUBLE -> statement.setNull(index, Types.DOUBLE);
        }
    }

    /**
     * general process of deleting rows
     *
     * @param rows the rows to be deleted
     * @param everything if true everything in the displayed javafx table will be deleted
     * @return true if successful
     */
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

    /**
     *
     * @param connection jdbc connection
     * @param rows the rows to be deleted. actually not all rows are needed and some deletions have no affect
     *             because everything is deleted already
     */
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

    /**
     * refreshes the javafx data table
     * @param table the javafx table
     * @return a list of all database data fpr a table converted into custom rows/cells
     */
    public ObservableList<CustomRow> updateDataTable(TableView<CustomRow> table) {
        List<? extends DbTableColumn> dbTableColumns = getTableColumns(dbTableColumnRepository);
        prepareTable(table, dbTableColumns, dbTableManger.getKeyColumns(), dbTableManger.getColumnOrder());
        return getAllRows(dbTableManger.getTableName(), dbTableColumns);
    }

    /**
     * filters rows by selection
     *
     * @param key the key column name (isin for stock/course, depotname for depots)
     * @param keyValue the key value
     * @param rows the rows to be filtered (normally all rows)
     * @return the filtered rows
     */
    public ObservableList<CustomRow> getRowsBySelection(String key, String keyValue, ObservableList<CustomRow> rows) {

        ObservableList<CustomRow> objects = FXCollections.observableArrayList();
        for (CustomRow row : rows) {
            if(row.getCells().containsKey(key)) {
                if (row.getCells().get(key).visualizedDataPropertyProperty().get().equals(keyValue)) {
                    objects.add(row);
                }
            }
        }
        return objects;
    }

    /**
     * gets all data rows for a specific table
     *
     * @param tableName the table that contains the data
     * @param columns the column entitys used for cell generation
     * @return all data rows as custom rows
     */
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

    /**
     *
     * @param repository defines which db table columns will we returned
     * @param <T> subclass of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn}
     * @return all column entities based on the repository
     */
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

                    setStatementKeys(stmt, keys);
                    fillByDataType( stmt, cell.getDbData(), cell.getDatatype(), 1);
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

    /**
     * customizes the javafx table to be ready for element insertion
     *
     * @param table the javafx selection table
     */
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

        table.setEditable(true);
    }

    public void updateStockSelectionTable(TableView<Stock> table) {
        table.getItems().addAll(stockRepository.findAll());
    }

    public void prepareDepotSelectionTable(TableView<Depot> table) {
        TableColumn<Depot, String> nameCol =  new TableColumn<>("Name");
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        nameCol.setEditable(false);
        table.getColumns().add(nameCol);
        table.setEditable(true);
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

    /**
     * used for the column deletion combo box
     *
     * @return all column that are allowed to be deleted given the current repository and manager
     */
    public List<? extends DbTableColumn>  getDbTableColumns() {
        var all = dbTableColumnRepository.findAll();
        all.removeIf(column -> dbTableManger.getReservedColumns().contains(column.getName()));
        return all;
    }

    public void addColumn(String colName, VisualDatatype visualDatatype) {
        dbTableManger.addColumn(colName.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_\\-äöüß]",""),
                                visualDatatype);
    }

    public boolean removeColumn(String colName) {
        return dbTableManger.removeColumn(colName);
    }
}
