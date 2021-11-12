package de.tud.inf.mmt.wmscrape.dynamicdb;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public abstract class DynamicDbManger {
    @Autowired
    DataSource dataSource;

    public ArrayList<String> getColumns(String tableName) {
        ArrayList<String> columns = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = '"+tableName+"';");

            while (results.next()) {
                columns.add(results.getString(1));
            }

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columns;
    }

    public boolean addColumnIfNotExists(String tableName, DynamicDbRepository repository , DbTableColumn column) {
        if(column == null || column.getColumnDatatype() == null || column.getName() == null) {
            return false;
        }

        try {
            if (columnExists(column.getName(), tableName)) {
                return false;
            }

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE "+tableName+" ADD " + column.getName() +
                    " " + column.getColumnDatatype().name()+ ";");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if(repository.findByName(column.getName()).isEmpty()) {
            repository.save(column);
        }

        return true;
    }

    protected boolean removeColumn(String columnName, String tableName, DynamicDbRepository repository) {
        try {
            if (!columnExists(columnName, tableName)) {
                return false;
            }

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE "+tableName+" DROP COLUMN " + columnName + ";");
            statement.close();
            connection.close();

            Optional<DbTableColumn> column = repository.findByName(columnName);
            column.ifPresent(repository::delete);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean columnExists(String columnName, String tableName){
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = '"+tableName+"';");
            while (results.next()) {
                if (results.getString(1).equals(columnName)) {
                    statement.close();
                    connection.close();
                    return true;
                }
            }
            statement.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return true;
        }
        return false;
    }

    public ColumnDatatype getColumnDataType(String columnName, String tableName){
        //https://www.tutorialspoint.com/java-resultsetmetadata-getcolumntype-method-with-example

        try {

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT " + columnName + " FROM "+tableName);
            int type = results.getMetaData().getColumnType(1);

            statement.close();
            connection.close();

            return switch (type) {
                case 91 -> ColumnDatatype.DATE;
                case 4 -> ColumnDatatype.INT;
//                case 2014 -> ColumnDatatype.DATETIME;
                case 93, 8 -> ColumnDatatype.DOUBLE;
                default -> ColumnDatatype.TEXT;
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean tableExists(String tableName) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SHOW TABLES;");

            while (results.next()) {
                String name = results.getString(1);
                if (name.equals(tableName)) {
                    return true;
                }
            }
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean initializeTable(String statementOrder) {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute(statementOrder);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
