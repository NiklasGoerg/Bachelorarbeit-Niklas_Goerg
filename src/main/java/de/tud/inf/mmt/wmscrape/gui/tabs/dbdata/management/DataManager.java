package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import de.tud.inf.mmt.wmscrape.dynamicdb.*;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.Depot;
import de.tud.inf.mmt.wmscrape.gui.tabs.depots.data.DepotRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
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
     *
     * the commented out code below had to be moved down to the actual implementation to allow
     * joining tables (used for the "r_par").
     *
     *         return repository.findAll();
     *
     * can therefore be used to add columns that normally don't fall into the administration of the current
     * column entity manager
     *
     * @param repository defines which db table columns will we returned
     * @param <T> subclass of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn}
     * @return all column entities based on the repository
     */
    protected abstract <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(@SuppressWarnings("unused") DbTableColumnRepository<T, Integer> repository);

    /**
     * moves the creation of the "select all" statement to the implementations to allow joining tables
     *
     * @return the sql statement used to fetch data for the data table. uses {@link DbTableColumn}s to extract the
     * data from the sql results.
     */
    protected abstract String getSelectionStatement(LocalDate startDate, LocalDate endDate);

    protected abstract String getSelectionStatementOnlyLatestRows();

    protected String getStartAndEndDateQueryPart(LocalDate startDate, LocalDate endDate, String dateColumnName) {
        if(startDate != null && endDate != null) {
            return String.format(" WHERE '%s' <= %s AND %s <= '%s'", startDate, dateColumnName, dateColumnName, endDate);
        } else if(startDate != null) {
            return String.format(" WHERE '%s' <= %s", startDate, dateColumnName);
        } else if(endDate != null) {
            return String.format(" WHERE %s <= '%s'", dateColumnName, endDate);
        }

        return "";
    }

    /**
     * allows adding table specific sorting
     *
     * @param dataTable the table the sorting will be set
     */
    protected abstract void setDataTableInitialSort(TableView<CustomRow> dataTable);

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
                                                          List<String> reserved, List<String> order, Map<String, Double> columnWidths) {

        for(DbTableColumn dbColumn : columns) {
            String colName = dbColumn.getName();
            ColumnDatatype datatype = dbColumn.getColumnDatatype();

            TableColumn<CustomRow, String> tableColumn = new TableColumn<>(colName);

            // binding the event handlers to the cell to register changes
            // allows removing or adding the datatype specific symbols like € or %
            tableColumn.addEventHandler(TableColumn.editStartEvent(), event -> {
                    CustomRow c = (CustomRow) event.getRowValue();
                    if(c != null) c.getCells().get(colName).onEditStartEvent();
            });
            tableColumn.addEventHandler(TableColumn.editCancelEvent(), event -> {
                CustomRow c = (CustomRow) event.getRowValue();
                if(c != null) c.getCells().get(colName).onEditCancelEvent();
            });
            tableColumn.addEventHandler(TableColumn.editCommitEvent(), event -> {
                CustomRow c = (CustomRow) event.getRowValue();
                if(c != null) c.getCells().get(colName).onEditCommitEvent(event);
            });

            // binding the custom cell property directly to the table cell
            tableColumn.setCellValueFactory(param -> param.getValue().getCells().get(colName).visualizedDataProperty());
            tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            setComparator(tableColumn, datatype);

            tableColumn.setPrefWidth(columnWidths.getOrDefault(colName, 150d));

            if(reserved.contains(colName)) tableColumn.setEditable(false);

            table.getColumns().add(tableColumn);
        }

        sortTableColumnsByName(table.getColumns(), order);
    }

    /**
     * sorts (horizontal) a list of table columns based on an ordered string list
     *
     * @param cols the javafx columns to be sorted
     * @param order the column names in order
     */
    private void sortTableColumnsByName(ObservableList<TableColumn<CustomRow, ?>> cols, List<String> order) {
        cols.sort((a, b) -> {
            String aText = a.getText();
            String bText = b.getText();

            boolean containsA = order.contains(aText);
            boolean containsB = order.contains(bText);

            if(!containsA && containsB) return 1;
            if(containsA && !containsB) return -1;
            if(!containsA) return 0;

            return Integer.compare(order.indexOf(aText), order.indexOf(bText));
        });
    }

    /**
     * adds sorting (vertical) column values based on the datatype
     *
     * @param column the column to be sorted
     * @param datatype the column datatype
     */
    private void setComparator(TableColumn<?, String> column, ColumnDatatype datatype){

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
                    case DATETIME -> {return Timestamp.valueOf(x).compareTo(Timestamp.valueOf(y));}
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

    public void setColumnOrder(List<String> columns) {
        dbTableManger.setColumnOrder(columns);
    }

    public void setColumnWidth(String columnName, Double width) { dbTableManger.setColumnWidth(columnName, width); }

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
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
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
    public ObservableList<CustomRow> updateDataTable(TableView<CustomRow> table, boolean loadEverything, LocalDate startDate, LocalDate endDate) {
        List<? extends DbTableColumn> dbTableColumns = getTableColumns(dbTableColumnRepository);
        prepareTable(table, dbTableColumns, dbTableManger.getNotEditableColumns(), dbTableManger.getColumnOrder(), dbTableManger.getColumnWidths());
        setDataTableInitialSort(table);
        return getRows(dbTableColumns, loadEverything, startDate, endDate);
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
                if (row.getCells().get(key).visualizedDataProperty().get().equals(keyValue)) {
                    objects.add(row);
                }
            }
        }
        return objects;
    }

    /**
     * gets all data rows for a specific table
     *
     * @param columns the column entitys used for cell generation
     * @return all data rows as custom rows
     */
    public ObservableList<CustomRow> getRows(List<? extends DbTableColumn> columns, boolean loadEverything, LocalDate startDate, LocalDate endDate) {

        ObservableList<CustomRow> allRows = FXCollections.observableArrayList();

        String queryStatement = loadEverything ? getSelectionStatement(startDate, endDate) : getSelectionStatementOnlyLatestRows();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(queryStatement);

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
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return allRows;
    }

    /**
     * saves those rows that have been marked as changed
     *
     * @param rows the changed rows
     * @return true if successful
     */
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
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
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
        TableColumn<Stock, String> sortCol =  new TableColumn<>("R_Par");

        nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
        isinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getIsin()));
        wknCol.setCellValueFactory(param -> param.getValue().wknProperty());
        typCol.setCellValueFactory(param -> param.getValue().stockTypeProperty());
        sortCol.setCellValueFactory(param -> param.getValue().sortOrderProperty());

        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        wknCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typCol.setCellFactory(TextFieldTableCell.forTableColumn());
        sortCol.setCellFactory(TextFieldTableCell.forTableColumn());

        setComparator(sortCol, ColumnDatatype.INTEGER);

        nameCol.setEditable(true);
        isinCol.setEditable(false);
        wknCol.setEditable(true);
        typCol.setEditable(true);
        sortCol.setEditable(true);

        table.getColumns().add(isinCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(wknCol);
        table.getColumns().add(typCol);
        table.getColumns().add(sortCol);

        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void updateDepotSelectionTable(TableView<Depot> table) {
        table.getItems().addAll(depotRepository.findAll());
    }

    public void saveStockListChanges(ObservableList<Stock> stocks) {
        stockRepository.saveAllAndFlush(stocks);
    }

    public void deleteStock(ObservableList<Stock> stock) {
        stockRepository.deleteAll(stock);
    }

    public void deleteDepot(ObservableList<Depot> depot) {
        depotRepository.deleteAll(depot);
    }

    public boolean createStock(String isin, String wkn, String name, String type, String sortOrder) {
        if(isin == null || isin.isBlank()) return false;

        if(stockRepository.findByIsin(isin).isPresent()) return false;
        stockRepository.saveAndFlush(new Stock(isin, wkn, name, type, Integer.parseInt(sortOrder)));
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

    /**
     * adds a synchronisation between the selection and the data tab columns
     *
     * @param dataTable the javafx data table
     * @param selectionTable the javafx selection table
     */
    public void addColumnSortSync(TableView<CustomRow> dataTable, TableView<Stock> selectionTable) {
        syncDataColToSelectCol( dataTable, selectionTable);
        syncSelectToDataCol( dataTable,  selectionTable);
    }

    /**
     * synchronizes in the direction dataTable -> selectionTable
     *
     * @param dataTable the javafx data table
     * @param selectionTable the javafx selection table
     */
    private void syncDataColToSelectCol(TableView<CustomRow> dataTable, TableView<Stock> selectionTable) {
        for (TableColumn<CustomRow, ?> dataColumn : dataTable.getColumns()) { // for every column in the data table
            dataColumn.sortTypeProperty().addListener((o, ov, nv) -> { // add a listener to the sort property
                if(ov == nv) return;

                for(TableColumn<Stock, ?> selectionColumn : selectionTable.getColumns()) {
                    // check for a matching column name inside the selection table
                    if(dataColumn.getText().equals(selectionColumn.getText().toLowerCase())) {
                        // set to the same sort type
                        selectionColumn.setSortType(nv);
                        selectionTable.getSortOrder().clear();
                        selectionTable.getSortOrder().add(selectionColumn);
                        break;
                    }
                }
                selectionTable.sort();
            });
        }
    }

    /**
    * synchronizes in the direction selectionTable -> dataTable
     *
     * @param dataTable the javafx data table
     * @param selectionTable the javafx selection table
     */
    private void syncSelectToDataCol(TableView<CustomRow> dataTable, TableView<Stock> selectionTable) {
        for(TableColumn<Stock, ?> selectionColumn : selectionTable.getColumns()) {
            selectionColumn.sortTypeProperty().addListener((o, ov, nv) -> {
                if(ov == nv) return;

                for (TableColumn<CustomRow, ?> dataColumn : dataTable.getColumns()) {

                    if(selectionColumn.getText().toLowerCase().equals(dataColumn.getText())) {
                        dataColumn.setSortType(nv);
                        dataTable.getSortOrder().clear();
                        dataTable.getSortOrder().add(dataColumn);
                        break;
                    }
                }

                dataTable.sort();
            });
        }
    }
}
