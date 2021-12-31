package de.tud.inf.mmt.wmscrape.dynamicdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DbTableManger {
    @Autowired DataSource dataSource;
    @Autowired TransactionTemplate transactionTemplate;

    public abstract boolean removeColumn(String colName);
    public abstract void addColumn(String colName, ColumnDatatype datatype);
    public abstract String getTableName();
    public abstract List<String> getKeyColumns();
    public abstract List<String> getReservedColumns();
    public abstract List<String> getColumnOrder();
    protected abstract void saveNewInRepository(String colName, ColumnDatatype datatype);
    public abstract PreparedStatement getPreparedDataStatement(String colName, Connection connection) throws SQLException ;

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
        }

        return columns;
    }

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
        }

        if(repository.findByName(colName).isEmpty()) {
            repository.save(column);
        }

    }

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
        }
        return true;
    }

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
        }
        return false;
    }

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
                case 4 -> ColumnDatatype.INTEGER;
//                case 2014 -> ColumnDatatype.DATETIME;
                case 93, 8 -> ColumnDatatype.DOUBLE;
                default -> ColumnDatatype.TEXT;
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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
        }
        return true;
    }

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

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

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
}
