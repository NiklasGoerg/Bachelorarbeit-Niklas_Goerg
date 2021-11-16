package de.tud.inf.mmt.wmscrape.dynamicdb;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.*;
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
            PreparedStatement pst = connection.prepareStatement("SELECT column_name FROM information_schema.columns WHERE table_name = ?;");
            pst.setString(1, tableName);
            ResultSet results = pst.executeQuery();

            while (results.next()) {
                columns.add(results.getString(1));
            }

            pst.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columns;
    }

    @SuppressWarnings( "unchecked" )
    public <T extends DynamicDbRepository> boolean addColumnIfNotExists(String tableName, T repository , DbTableColumn column) {
        if(column == null || column.getColumnDatatype() == null || column.getName() == null) {
            return false;
        }

        try {
            if (columnExists(column.getName(), tableName)) {
                return false;
            }

            Connection connection = dataSource.getConnection();
            PreparedStatement pst = connection.prepareStatement("SET @table := ?, @col := ?, @type := ?;");
            pst.setString(1, tableName);
            pst.setString(2, column.getName());
            pst.setString(3,  column.getColumnDatatype().name());
            pst.execute();
            pst.execute("SET @sql := CONCAT(\"ALTER TABLE \", @table, \" ADD \", @col, \" \", @type, \";\");");
            pst.execute("PREPARE stmt FROM @sql;");
            pst.execute("EXECUTE stmt;");
            pst.close();
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

    @SuppressWarnings( "unchecked" )
    protected <T extends DynamicDbRepository> boolean removeColumn(String columnName, String tableName, T repository) {
        try {
            if (!columnExists(columnName, tableName)) {
                return false;
            }

            Connection connection = dataSource.getConnection();
            PreparedStatement pst = connection.prepareStatement("SET @table := ?, @col := ?;");
            pst.setString(1, tableName);
            pst.setString(2, columnName);
            pst.execute();
            pst.execute("SET @sql := CONCAT(\"ALTER TABLE \", @table, \" DROP COLUMN \", @col, \";\");");
            pst.execute("PREPARE stmt FROM @sql;");
            pst.execute("EXECUTE stmt;");
            pst.close();
            connection.close();

            Optional column = repository.findByName(columnName);
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


            PreparedStatement pst = connection.prepareStatement("SET @table := ?, @col := ?;");
            pst.setString(1, tableName);
            pst.setString(2, columnName);
            pst.execute();
            pst.execute("SET @sql := CONCAT(\"SELECT \", @col, \" FROM \", @table, \";\");");
            pst.execute("PREPARE stmt FROM @sql;");
            ResultSet results = pst.executeQuery("EXECUTE stmt;");
            int type = results.getMetaData().getColumnType(1);

            pst.close();
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
                    statement.close();
                    connection.close();
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

    public abstract PreparedStatement getPreparedStatement(String dbColName, Connection connection) throws SQLException;
    public abstract void removeColumn(String columnName);
}
