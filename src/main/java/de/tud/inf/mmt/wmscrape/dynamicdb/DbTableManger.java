package de.tud.inf.mmt.wmscrape.dynamicdb;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * provides the basic functions for db table manipulation
 */
public abstract class DbTableManger {
    @Autowired DataSource dataSource;
    @Autowired TransactionTemplate transactionTemplate;

    /**
     * removes a column from the respective table
     * @param colName the name of the column to be removed
     * @return true if successful
     */
    public abstract boolean removeColumn(String colName);

    /**
     * adds a column from the respective table
     * @param colName the name of the column to be added
     * @param visualDatatype the datatype that will be used to represent the value of the column will be translated into a {@link ColumnDatatype}
     */
    public abstract void addColumn(String colName, VisualDatatype visualDatatype);

    /**
     * @return the db table name
     */
    public abstract String getTableName();

    /**
     * get the key column names from the table.
     * used in the data tab to disallow modification
     * @return the keys as string value
     */
    public abstract List<String> getNotEditableColumns();

    /**
     * reserved columns have some special meaning and should not be removed.
     * used inside the data tab to disallow removing of columns
     * @return the reserved column names
     */
    public abstract List<String> getReservedColumns();

    /**
     * default column order defines the displayed column order in case the user hasn't rearanged them
     * @return the column names in a list
     */
    public abstract List<String> getDefaultColumnOrder();

    /**
     * column order defines the displayed column order in some javaFX tables
     * @return the column names in a list
     */
    public List<String> getColumnOrder() {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        String columnOrder = properties.getProperty(getTableName() + "ColumnOrder", null);

        if(columnOrder == null) {
            return getDefaultColumnOrder();
        } else {
            return List.of(columnOrder.split(","));
        }
    }

    /**
     * used for persisting the column order
     * @param columns the column order which should be saved to the .properties file
     */
    public void setColumnOrder(List<String> columns) {
        Properties properties = new Properties();
        var columnsString = String.join(",", columns);

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            properties.setProperty(getTableName() + "ColumnOrder", columnsString);
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * used for retrieving the column widths from the user.properties file
     */
    public Map<String, Double> getColumnWidths() {
        Map<String, Double> columnWidths = new HashMap<>();
        Properties properties = new Properties();

        try {

            properties.load(new FileInputStream("src/main/resources/user.properties"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        List<String> columns = getColumnOrder();

        for (String column : columns) {
            try {
                var propertiesWidth = properties.getProperty(getTableName() + column + "Width", null);

                if(propertiesWidth != null) {
                    var width = Double.valueOf(propertiesWidth);
                    columnWidths.put(column, width);
                }
            } catch (NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }

        return columnWidths;
    }

    /**
     * used for setting a specific column width
     * @param columnName the column which width should be updated
     * @param width the width the user has set the column to
     */
    public void setColumnWidth(String columnName, Double width) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            properties.setProperty(getTableName() + columnName + "Width", String.valueOf(width));
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * used for storing column entities in the right repository
     * @param colName the name of the column
     * @param datatype the sql-datatype of the column ({@link de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype})
     */
    protected abstract void saveNewInRepository(String colName, ColumnDatatype datatype);

    /**
     * used by some methods to create a prepared statement which only contains the keys and one data column to
     * insert data into the database
     * @param colName the name of the column
     * @param connection a sql-connection
     * @return the prepared statement ready to be filled with data and execute
     * @throws SQLException preparing a statement can cause an exception
     */
    public abstract PreparedStatement getPreparedDataStatement(String colName, Connection connection) throws SQLException ;

    /**
     * @param tableName name of the db table from which to get the columns
     * @return all columns from the physical database
     */
    public ArrayList<String> getColumns(String tableName) {
        if(tableName == null) return null;
        ArrayList<String> columns = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()){

            PreparedStatement pst = connection.prepareStatement("SELECT column_name FROM information_schema.columns WHERE table_name = ?;");
            pst.setString(1, tableName);
            ResultSet results = pst.executeQuery();

            while (results.next()) {
                columns.add(results.getString(1));
            }

            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return columns;
    }

    /**
     * adds a physical db column to a table if it doenst exists
     * @param tableName the name of the db table
     * @param repository the responsible repository for the table column entities
     * @param column a column entity for which the physical representation will be created
     * @param <T> a subclass of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn}
     */
    protected  <T extends DbTableColumnRepository<? extends DbTableColumn, Integer>> void addColumnIfNotExists(
            String tableName, T repository , DbTableColumn column) {


        if(column == null || column.getColumnDatatype() == null || column.getName() == null) {
            return;
        }

        String colName = column.getName()
                                .trim()
                                .toLowerCase()
                                .replaceAll("[^a-zA-Z0-9_\\-äöüß]","");

        if(colName.isBlank()) return;


        try (Connection connection = dataSource.getConnection()) {
            if (columnExists(colName, tableName)) return;

            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE `"+tableName+"` ADD `"+colName+"` "+column.getColumnDatatype().name());
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        if(repository.findByName(colName).isEmpty()) {
            repository.save(column);
        }

    }

    /**
     * does the basic operations to delete a column. the exact details are provided by the subclasses
     *
     * @param columnName the name of the column to be deleted
     * @param tableName the name of the table where the column exists in
     * @param repository the responsible repository subclass
     * @param <T> a subclass of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn}
     * @return true if successful
     */
    protected <T extends DbTableColumnRepository<? extends DbTableColumn, Integer>> boolean removeAbstractColumn(
            String columnName, String tableName, T repository) {

        if(columnName == null || tableName == null || repository == null) return false;

        Optional<? extends DbTableColumn> column = repository.findByName(columnName);
        column.ifPresent(repository::delete);

        try (Connection connection = dataSource.getConnection()){
            if (!columnExists(columnName, tableName)) return true;

            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE `"+tableName+"` DROP COLUMN `"+columnName+"`");
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
        return true;
    }

    /**
     * checks if a physical column exists in the db table
     * @param columnName the column name to be checked
     * @param tableName the name of the table where the column exists in
     * @return true if it exists
     */
    public boolean columnExists(String columnName, String tableName){
        if(columnName == null || tableName == null) return true;

        try (Connection connection = dataSource.getConnection()){
            PreparedStatement pst = connection.prepareStatement("SELECT column_name FROM information_schema.columns WHERE table_name = ?;");
            pst.setString(1, tableName);
            ResultSet results = pst.executeQuery();

            while (results.next()) {
                if (results.getString(1).equals(columnName)) {
                    pst.close();
                    connection.close();
                    return true;
                }
            }
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
        return false;
    }

    /**
     * gets the datatype of a column in the database
     * @param columnName the column name to be checked
     * @param tableName the name of the table where the column exists in
     * @return the column sql datatype
     */
    public ColumnDatatype getColumnDataType(String columnName, String tableName){
        //https://www.tutorialspoint.com/java-resultsetmetadata-getcolumntype-method-with-example

        if(columnName == null || tableName == null ) return null;

        try (Connection connection = dataSource.getConnection()){

            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT `"+columnName+"` FROM `"+tableName+"`");

            int type = results.getMetaData().getColumnType(1);

            statement.close();
            connection.close();

            return switch (type) {
                case 91 -> ColumnDatatype.DATE;
                case 93 -> ColumnDatatype.DATETIME; // atm only an addition for the depot transaction ate column -> only necessary for comparing in the data tab
                case 4 -> ColumnDatatype.INTEGER;
                case 8 -> ColumnDatatype.DOUBLE;
                default -> ColumnDatatype.TEXT;
            };
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
        return null;
    }

    /**
     * checks if a table exists in the database
     * @param tableName the tale name to be checked
     * @return true if it doesn't exist
     */
    public boolean tableDoesNotExist(String tableName) {

        if(tableName == null) return false;

        try (Connection connection = dataSource.getConnection()){
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SHOW TABLES;");

            while (results.next()) {
                String name = results.getString(1);
                if (name.equals(tableName)) {
                    statement.close();
                    connection.close();
                    return false;
                }
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
        return true;
    }

    /**
     * executes a prepared statement
     * @param statementOrder the string of the to be executed sql statement
     */
    public void executeStatement(String statementOrder) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute(statementOrder);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return a jdbc connection
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * creates all column entities for a given table and stores them in the repository. Entities which do no longer
     * have a physical representation are deleted
     *
     * @param repository the responsible repository subclass
     * @param tableName the db table name
     * @param <T> a subclass of {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn}
     */
    protected <T extends DbTableColumnRepository<? extends DbTableColumn, Integer>> void initTableColumns(T repository, String tableName) {
        // the column names where a representation in db_table_column_exists
        ArrayList<String> representedColumns = new ArrayList<>();
        for(DbTableColumn column : repository.findAll()) {
            representedColumns.add(column.getName());
        }

        for(String colName : getColumns(tableName)) {
            if(!representedColumns.contains(colName)) {
                // add new representation
                ColumnDatatype datatype = getColumnDataType(colName, tableName);
                if(datatype == null) continue;
                saveNewInRepository(colName, datatype);
            } else  {
                // representation exists
                representedColumns.remove(colName);
            }
        }

        // removing references that do not exist anymore
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                representedColumns.forEach(repository::deleteByName);
            }
        });
    }

    /**
     * translates a mysql datatype to a default visual datatype
     *
     * @param datatype the mysql datatype
     * @return the visual default datatype
     */
    public static VisualDatatype translateVisualDatatype(ColumnDatatype datatype) {
        switch (datatype) {
            case TEXT -> {return VisualDatatype.Text;}
            case DATE, DATETIME -> {return VisualDatatype.Datum;}
            case DOUBLE -> {return VisualDatatype.Double;}
            case INTEGER -> {return VisualDatatype.Int;}
        }
        throw new RuntimeException("Ungültiger Datentyp angegeben");
    }

    /**
     * translates a visual datatype to a mysql datatype
     *
     * @param datatype the visual datatype
     * @return the mysql datatype
     */
    public static ColumnDatatype translateDataType(VisualDatatype datatype) {
        switch (datatype) {
            case Datum -> {return ColumnDatatype.DATE;}
            case Int -> {return ColumnDatatype.INTEGER;}
            case Text -> {return ColumnDatatype.TEXT;}
            case Double, Prozent, Euro, Doller -> {return ColumnDatatype.DOUBLE;}
        }
        throw new RuntimeException("Ungültiger Datentyp angegeben");
    }
}
