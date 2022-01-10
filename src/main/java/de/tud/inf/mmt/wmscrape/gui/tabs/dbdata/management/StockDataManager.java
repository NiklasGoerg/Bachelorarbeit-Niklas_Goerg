package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockDataManager extends StockAndCourseManager {

    @Autowired
    StockColumnRepository stockColumnRepository;
    @Autowired
    StockTableManager stockDataDbManager;

    @Override
    protected void setColumnRepositoryAndManager(){
        dbTableColumnRepository = stockColumnRepository;
        dbTableManger = stockDataDbManager;
    }

    @Override
    protected <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(DbTableColumnRepository<T, Integer> repository) {
        List<StockColumn> cols = stockColumnRepository.findAll();
        cols.add(new StockColumn("r_par", VisualDatatype.Int));
        return cols;
    }

    @Override
    protected String getSelectionStatement() {
        // for every stock in the stock table exists a stock so there can't be any null values
        // adds the r_par column to the table
        return "SELECT WP.r_par , SD.* FROM wertpapier WP RIGHT OUTER JOIN `"+StockTableManager.TABLE_NAME+"` SD ON WP.isin = SD.isin";
    }
}
