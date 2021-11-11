package de.tud.inf.mmt.wmscrape.gui.tabs.datatab.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.stock.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.stock.StockDataTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.stock.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    DynamicDbManger dynamicDbManger;

    @PostConstruct
    private void initStockData() {
        // the stock data table is not managed by spring
        // and has to be initialized by myself

        if (!dynamicDbManger.tableExists("stammdaten")) {
            dynamicDbManger.initializeTable("CREATE TABLE IF NOT EXISTS stammdaten ( isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(StockDataTableColumn column : stockDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : dynamicDbManger.getColumns("stammdaten")) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = dynamicDbManger.getColumnDataType(colName, "stammdaten");
                stockDataColumnRepository.save(new StockDataTableColumn(colName, datatype));
            }
        }

        dynamicDbManger.addColumnIfNotExists("stammdaten", stockDataColumnRepository,new StockDataTableColumn("wkn", ColumnDatatype.TEXT));
        dynamicDbManger.addColumnIfNotExists("stammdaten", stockDataColumnRepository,new StockDataTableColumn("name", ColumnDatatype.TEXT));
        dynamicDbManger.addColumnIfNotExists("stammdaten", stockDataColumnRepository,new StockDataTableColumn("typ", ColumnDatatype.TEXT));
        dynamicDbManger.addColumnIfNotExists("stammdaten", stockDataColumnRepository,new StockDataTableColumn("gruppen_id", ColumnDatatype.INT));
    }


    public void removeColumn(String columnName) {
        Optional<StockDataTableColumn> column = stockDataColumnRepository.findByName(columnName);
        if(column.isPresent()) {
            column.get().setExcelCorrelations(new ArrayList<>());
            dynamicDbManger.removeColumn(column.get().getName(),"stammdaten", stockDataColumnRepository);
        }
    }

    public void createMissingStocks() {

        Statement statement;
        try {
            Connection connection = dynamicDbManger.getConnection();
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

    public Connection getConnection() {
        return dynamicDbManger.getConnection();
    }
}
