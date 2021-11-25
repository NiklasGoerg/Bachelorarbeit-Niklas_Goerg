package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service
public class StockDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "stammdaten";
    public static final ArrayList<String> RESERVED_COLUMNS = new ArrayList<>(Arrays.asList("datum","isin"));
    public static final ArrayList<String> COLUMN_ORDER = new ArrayList<>(Arrays.asList("datum","isin","wkn","name","typ","gruppe"));

    @Autowired
    StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    StockRepository stockRepository;

    @PostConstruct
    private void initStockData() {
        // the stock data table is not managed by spring
        // and has to be initialized by myself

        if (!tableExists(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                stockDataColumnRepository.save(new StockDataDbTableColumn(colName, datatype));
            }
        }

        initColumn("wkn", ColumnDatatype.TEXT);
        initColumn("name", ColumnDatatype.TEXT);
        initColumn("typ", ColumnDatatype.TEXT);
        initColumn("gruppe", ColumnDatatype.INTEGER);
    }

    private boolean initColumn(String name, ColumnDatatype datatype) {
        return addColumnIfNotExists(TABLE_NAME, stockDataColumnRepository, new StockDataDbTableColumn(name, datatype));
    }


    public void createMissingStocks() {

        Statement statement;
        try {
            Connection connection = getConnection();
            statement = connection.createStatement();
            // isin, wkn, name columns are created at start if not existing
            ResultSet resultSet = statement.executeQuery("SELECT isin, wkn, name, gruppe, typ FROM "+TABLE_NAME+";");

            while (resultSet.next()) {
                String isin = resultSet.getString("isin");

                if(stockRepository.findByIsin(isin).isEmpty()) {
                    Stock stock = new Stock(isin,
                            resultSet.getString("wkn"),
                            resultSet.getString("name"),
                            resultSet.getInt("gruppe"),
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

    public PreparedStatement getPreparedStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO "+TABLE_NAME+" (isin, datum, " + dbColName + ") VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";
        return connection.prepareStatement(sql);
    }

    @Override
    public void removeColumn(String columnName) {
        Optional<StockDataDbTableColumn> column = stockDataColumnRepository.findByName(columnName);
        if(column.isPresent()) {
            column.get().setExcelCorrelations(new ArrayList<>());
            super.removeColumn(column.get().getName(), TABLE_NAME, stockDataColumnRepository);
        }
    }
}
