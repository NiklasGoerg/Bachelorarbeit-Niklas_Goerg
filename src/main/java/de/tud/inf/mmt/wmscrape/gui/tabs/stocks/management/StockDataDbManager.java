package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class StockDataDbManager {

    @Autowired
    StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    DataSource dataSource;

    @PostConstruct
    private void initStockData() {
        // the stock data table is not managed by spring
        // and has to be initialized by myself

        if (!tableExists()) {
            initializeTable();
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(StockDataTableColumn column : stockDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns()) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName);
                StockDataTableColumn col = new StockDataTableColumn(colName, datatype);
                stockDataColumnRepository.save(col);
            }
        }

        if(!columnExists("wkn")) {
            addColumn("wkn", ColumnDatatype.TEXT);
        }
        if(!columnExists("name")) {
            addColumn("name", ColumnDatatype.TEXT);
        }
        if(!columnExists("typ")) {
            addColumn("typ", ColumnDatatype.TEXT);
        }
        if(!columnExists("gruppen_id")) {
            addColumn("gruppen_id", ColumnDatatype.INT);
        }
    }

    private ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'stammdaten';");

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

    private boolean addColumn(String columnName, ColumnDatatype columnDatatype) {
        try {
            if (columnExists(columnName)) {
                return false;
            }

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE stammdaten ADD " + columnName + " " + columnDatatype.name()+ ";");
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if(stockDataColumnRepository.findByName(columnName).isEmpty()) {
            StockDataTableColumn column = new StockDataTableColumn(columnName,columnDatatype);
            stockDataColumnRepository.save(column);
        }

        return true;
    }

    private boolean removeColumn(String columnName) {
        try {
            if (!columnExists(columnName)) {
                return false;
            }

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("ALTER TABLE stammdaten DROP COLUMN " + columnName + ";");
            statement.close();
            connection.close();

            Optional<StockDataTableColumn> column = stockDataColumnRepository.findByName(columnName);
            if(column.isPresent()) {
                // fix for not working orphan removal
                column.get().setExcelCorrelations(new ArrayList<>());
                stockDataColumnRepository.delete(column.get());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean columnExists(String columnName){
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'stammdaten';");
            while (results.next()) {
                if (results.getString(1).equals(columnName)) {
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

    private ColumnDatatype getColumnDataType(String columnName){
        //https://www.tutorialspoint.com/java-resultsetmetadata-getcolumntype-method-with-example

        try {

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT " + columnName + " FROM stammdaten");
            int type = results.getMetaData().getColumnType(1);

//            ResultSet results = statement.executeQuery(
//                    "SELECT data_type FROM information_schema.columns WHERE table_name = 'stammdaten' " +
//                            "AND column_name = '" + columnName + "';");
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

    private boolean tableExists() {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SHOW TABLES;");

            while (results.next()) {
                String name = results.getString(1);
                if (name.equals("stammdaten")) {
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

    private boolean initializeTable() {
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS stammdaten ( isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
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


    public void createMissingStocks() {

        Statement statement;
        try {
            Connection connection = dataSource.getConnection();
            statement = connection.createStatement();
            // isin, wkn, name columns are created at start if not existing
            ResultSet resultSet = statement.executeQuery("SELECT isin, wkn, name, gruppen_id, typ FROM stammdaten;");

            while (resultSet.next()) {
                String isin = resultSet.getString("isin");

                if(stockRepository.findByIsin(isin).isEmpty()) {
                    Stock stock = new Stock(isin,
                            resultSet.getString("wkn"),
                            resultSet.getString("name"),
                            resultSet.getInt("gruppen_id"),
                            resultSet.getString("typ"));
                    stockRepository.save(stock);
                }
            }

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
