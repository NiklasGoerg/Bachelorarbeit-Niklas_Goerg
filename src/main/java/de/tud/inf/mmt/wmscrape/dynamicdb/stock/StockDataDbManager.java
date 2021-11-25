package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "stammdaten";
    public static final List<String> RESERVED_COLUMNS = List.of("datum","isin");
    public static final List<String> COLUMN_ORDER = List.of("datum","isin","wkn","name","typ");

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


        // todo remove
        initColumn("url_1", ColumnDatatype.TEXT);
        initColumn("url_2", ColumnDatatype.TEXT);
        initColumn("kurs", ColumnDatatype.DOUBLE);
        initColumn("gruppe", ColumnDatatype.INTEGER);
        initColumn("Std_Datum", ColumnDatatype.DATE);
    }

    private boolean initColumn(String name, ColumnDatatype datatype) {
        return addColumnIfNotExists(TABLE_NAME, stockDataColumnRepository, new StockDataDbTableColumn(name, datatype));
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
