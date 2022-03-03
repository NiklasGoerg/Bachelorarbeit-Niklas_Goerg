package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StockDataManager extends StockAndCourseManager {

    @Autowired
    StockColumnRepository stockColumnRepository;
    @Autowired
    StockTableManager stockDataDbManager;

    @SuppressWarnings("unused")
    @Override
    protected void setColumnRepositoryAndManager(){
        dbTableColumnRepository = stockColumnRepository;
        dbTableManger = stockDataDbManager;
    }

    @Override
    protected <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(DbTableColumnRepository<T, Integer> repository) {
        List<StockColumn> cols = stockColumnRepository.findAll();
        cols.add(new StockColumn("r_par", VisualDatatype.Int));
        cols.add(new StockColumn("name", VisualDatatype.Text));
        cols.add(new StockColumn("wkn", VisualDatatype.Text));
        cols.add(new StockColumn("typ", VisualDatatype.Text));
        return cols;
    }

    @Override
    protected String getSelectionStatement(LocalDate startDate, LocalDate endDate) {
        // for every stock in the stock table exists a stock so there can't be any null values
        // adds the r_par column to the table
        return "SELECT WP.* , SD.* FROM wertpapier WP RIGHT OUTER JOIN `"+StockTableManager.TABLE_NAME+"` SD ON WP.isin = SD.isin" + getStartAndEndDateQueryPart(startDate, endDate, "datum");
    }

    @Override
    protected String getSelectionStatementOnlyLatestRows() {
        return "SELECT WP.* , SD.* FROM wertpapier WP RIGHT OUTER JOIN(select * from `"+StockTableManager.TABLE_NAME+"` WP inner join ( select isin as isin_kd, max(datum) as MaxDate from `"+StockTableManager.TABLE_NAME+"` group by isin) WPL on WP.isin = WPL.isin_kd and WP.datum = WPL.MaxDate ) SD ON WP.isin = SD.isin";
    }
}
