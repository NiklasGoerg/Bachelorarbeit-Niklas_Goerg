package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "stammdaten";
    public static final List<String> RESERVED_COLUMNS = List.of("datum","isin");
    public static final List<String> COLUMN_ORDER = List.of("datum","isin","wkn","name","typ");

    @Autowired
    StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    StockRepository stockRepository;
    @Autowired
    TransactionTemplate transactionTemplate;

    @PostConstruct
    private void initStockData() {
        // the stock data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        // the column names where a representation in db_table_column_exists
        ArrayList<String> representedColumns = new ArrayList<>();
        for(StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            representedColumns.add(column.getName());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!representedColumns.contains(colName)) {
                // add new representation
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                stockDataColumnRepository.saveAndFlush(new StockDataDbTableColumn(colName, datatype));
            } else {
                // representation exists
                representedColumns.remove(colName);
            }
        }

        // removing references that do not exist anymore
        removeRepresentation(representedColumns, stockDataColumnRepository);


        // todo remove
        addColumn("url_1", ColumnDatatype.TEXT);
        addColumn("url_2", ColumnDatatype.TEXT);
        addColumn("kurs", ColumnDatatype.DOUBLE);
        addColumn("gruppe", ColumnDatatype.INTEGER);
        addColumn("Std_Datum", ColumnDatatype.DATE);
    }

    public PreparedStatement getPreparedStatement(String dbColName, Connection connection) throws SQLException {
        String sql = "INSERT INTO "+TABLE_NAME+" (isin, datum, " + dbColName + ") VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";
        return connection.prepareStatement(sql);
    }

    public void removeColumn(String columnName) {
//        Optional<StockDataDbTableColumn> column = stockDataColumnRepository.findByName(columnName);
//        if(column.isPresent()) {
//            column.get().setExcelCorrelations(null);
//        }
        super.removeAbstractColumn(columnName, TABLE_NAME, stockDataColumnRepository);
    }

    @Override
    public void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, stockDataColumnRepository, new StockDataDbTableColumn(colName, datatype));
    }
}
