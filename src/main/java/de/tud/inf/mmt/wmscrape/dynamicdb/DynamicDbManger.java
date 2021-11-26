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

public abstract class DynamicDbManger {
    @Autowired
    DataSource dataSource;
    @Autowired
    TransactionTemplate transactionTemplate;

    public ArrayList<String> getColumns(String tableName) {
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


    protected  <T extends DynamicDbRepository<? extends DbTableColumn, Integer>> void addColumnIfNotExists(
            String tableName, T repository , DbTableColumn column) {

        if(column == null || column.getColumnDatatype() == null || column.getName() == null) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            if (columnExists(column.getName(), tableName)) return;

            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE `"+tableName+"` ADD `"+column.getName()+"` "+column.getColumnDatatype().name());
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if(repository.findByName(column.getName()).isEmpty()) {
            repository.save(column);
        }

    }

    protected <T extends DynamicDbRepository<? extends DbTableColumn, Integer>> boolean removeAbstractColumn(
            String columnName, String tableName, T repository) {

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

    public void initializeTable(String statementOrder) {
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

    protected <T extends  DynamicDbRepository<? extends DbTableColumn, Integer>> void removeOldRepresentation(List<String> names, T repository) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
                names.forEach(repository::deleteByName);
            }
        });
    }

    public abstract boolean removeColumn(String colName);
    public abstract void addColumn(String colName, ColumnDatatype datatype);
    public abstract String getTableName();
    public abstract List<String> getReservedColumns();
    public abstract List<String> getColumnOrder();
}
