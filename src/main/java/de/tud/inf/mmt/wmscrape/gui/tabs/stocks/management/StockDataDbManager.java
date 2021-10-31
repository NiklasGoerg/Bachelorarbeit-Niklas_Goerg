package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
public class StockDataDbManager {

    @Autowired
    StockDataColumnRepository stockDataColumnRepository;
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

        try {
            if(!columnExists("wkn")) {
                addColumn("wkn", ColumnDatatype.TEXT);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }

        StockDataTableColumn column = new StockDataTableColumn(columnName,columnDatatype);
        stockDataColumnRepository.save(column);
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

            StockDataTableColumn column = stockDataColumnRepository.findByName(columnName);
            // fix for not working orphan removal
            column.setExcelCorrelations(new ArrayList<>());
            stockDataColumnRepository.delete(column);
        } catch (SQLException e) {
            e.printStackTrace();
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
//                case 2014:
//                    return ColumnDatatype.DATETIME;
                case 8:
                    return ColumnDatatype.DOUBLE;
                default:
                    return ColumnDatatype.TEXT;
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean initializeTable() {
        try {
            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS stammdaten ( isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Transactional
    public boolean insertToStockTable(String isin, Date date, PreparedStatement statement, String data, ColumnDatatype datatype) {

//        if(!date.matches("^[1-9][0-9]{3}\\-[0-9]{2}\\-[0-9]{2}$")) {
//            return false;
//        }
        if (isin.length() >= 50) {
            return false;
        }

        try {
            statement.setString(1,isin);
            statement.setDate(2,date);

            switch (datatype) {
                case DATE:
                    LocalDate dataToDate = LocalDate.parse(data);
                    statement.setDate(3, Date.valueOf(dataToDate));
                    break;
                case TEXT:
                    statement.setString(3,data);
                    break;
                case INT:
                    statement.setInt(3, Integer.parseInt(data));
                    break;
                case DOUBLE:
                    statement.setDouble(3, Double.parseDouble(data));
                    break;
                default:
                    break;
                }

            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public PreparedStatement getPreparedStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO stammdaten (isin, datum, "+dbColName+") VALUES(?,?,?) ON DUPLICATE KEY UPDATE "+dbColName+"=VALUES("+dbColName+");";
        PreparedStatement pst = connection.prepareCall(sql);
        return pst;
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
