package de.tud.inf.mmt.wmscrape.dynamicdb;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

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


    public <T extends DynamicDbRepository<? extends DbTableColumn, Integer>> void addColumnIfNotExists(
            String tableName, T repository , DbTableColumn column) {

        if(column == null || column.getColumnDatatype() == null || column.getName() == null) {
            return;
        }

        try {
            if (columnExists(column.getName(), tableName)) {
                return;
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
            return;
        }

        if(repository.findByName(column.getName()).isEmpty()) {
            repository.save(column);
        }

    }

    protected <T extends DynamicDbRepository<? extends DbTableColumn, Integer>> void removeAbstractColumn(String columnName, String tableName, T repository) {
        try {
            if (!columnExists(columnName, tableName)) {
                return;
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

            Optional<? extends DbTableColumn> column = repository.findByName(columnName);
            column.ifPresent(repository::delete);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        try {
            Connection connection = dataSource.getConnection();
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
            connection.close();
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

    protected abstract void removeColumn(String colName);
}
