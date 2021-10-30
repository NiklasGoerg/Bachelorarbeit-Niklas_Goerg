package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@Service
public class StockDataDbManager {

    @Autowired
    StockColumnRepository stockColumnRepository;
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
        for(StockDataTableColumn column : stockColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns()) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName);
                StockDataTableColumn col = new StockDataTableColumn(colName, datatype);
                stockColumnRepository.save(col);
            }
        }
    }

    private ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();

        try {
            Statement statement = dataSource.getConnection().createStatement();
            ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'stammdaten';");

            while (results.next()) {
                columns.add(results.getString(1));
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println(e);
        }

        return columns;
    }

    private boolean addColumn(String columnName, ColumnDatatype columnDatatype) {
        try {
            if (columnExists(columnName)) {
                return false;
            }

            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("ALTER TABLE stammdaten ADD " + columnName + " " + columnDatatype.name()+ ";");
            statement.close();
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }

        StockDataTableColumn column = new StockDataTableColumn(columnName,columnDatatype);
        stockColumnRepository.save(column);
        return true;
    }

    private boolean removeColumn(String columnName) {
        try {
            if (!columnExists(columnName)) {
                return false;
            }

            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("ALTER TABLE stammdaten DROP COLUMN " + columnName + ";");
            statement.close();

            stockColumnRepository.deleteAll(stockColumnRepository.findAllByName(columnName));
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    private boolean columnExists(String columnName) throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();
        ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'stammdaten';");

        while (results.next()) {
            if (results.getString(1).equals(columnName)) {
                return true;
            }
        }
        statement.close();
        return false;
    }

    private ColumnDatatype getColumnDataType(String columnName){
        //https://www.tutorialspoint.com/java-resultsetmetadata-getcolumntype-method-with-example

        try {
//            if (!columnExists(columnName)) {
//                return null;
//            }
            Statement statement = dataSource.getConnection().createStatement();
            ResultSet results = statement.executeQuery("SELECT " + columnName + " FROM stammdaten");
            int type = results.getMetaData().getColumnType(1);

//            ResultSet results = statement.executeQuery(
//                    "SELECT data_type FROM information_schema.columns WHERE table_name = 'stammdaten' " +
//                            "AND column_name = '" + columnName + "';");
            statement.close();

            switch (type) {
                case 91:
                    return ColumnDatatype.DATE;
                case 4:
                    return ColumnDatatype.INT;
                case 93:
                case 2014:
                    return ColumnDatatype.DATETIME;
                case 8:
                    return ColumnDatatype.DOUBLE;
                default:
                    return ColumnDatatype.TEXT;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    private boolean tableExists() {
        try {
            Statement statement = dataSource.getConnection().createStatement();
            ResultSet results = statement.executeQuery("SHOW TABLES;");

            while (results.next()) {
                String name = results.getString(1);
                if (name.equals("stammdaten")) {
                    return true;
                }
            }
            statement.close();

        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
        return false;
    }

    private boolean initializeTable() {
        try {
            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS stammdaten ( isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }
}
