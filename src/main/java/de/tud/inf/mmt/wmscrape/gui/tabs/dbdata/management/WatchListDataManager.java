package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.watchlist.WatchListColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.watchlist.WatchListTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WatchListDataManager extends StockAndCourseManager {
    @Autowired
    WatchListTableManager watchListTableManager;
    @Autowired
    WatchListColumnRepository watchListColumnRepository;


    @Override
    protected void setColumnRepositoryAndManager() {
        dbTableManger = watchListTableManager;
        dbTableColumnRepository = watchListColumnRepository;
    }

    @Override
    protected <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(DbTableColumnRepository<T, Integer> repository) {
        return watchListColumnRepository.findAll();
    }

    @Override
    protected String getSelectionStatement(LocalDate startDate, LocalDate endDate) {
        // for every stock in the course table exists a stock so there can't be any null values
        // adds the r_par column to the table
        return "SELECT * FROM " + WatchListTableManager.TABLE_NAME + getStartAndEndDateQueryPart(startDate, endDate, "datum");
    }

    @Override
    protected String getSelectionStatementOnlyLatestRows() {
        return "SELECT WL.* FROM " + WatchListTableManager.TABLE_NAME + " WL INNER JOIN (SELECT isin AS isin_kd, max(datum) AS MaxDate FROM " + WatchListTableManager.TABLE_NAME + " GROUP BY isin) WPL WHERE WL.isin = WPL.isin_kd AND WL.datum = WPL.MaxDate";
    }
}
