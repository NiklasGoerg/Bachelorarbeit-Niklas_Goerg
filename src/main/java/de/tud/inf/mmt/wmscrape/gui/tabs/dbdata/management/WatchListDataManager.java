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
        return "SELECT * FROM watch_list" + getStartAndEndDateQueryPart(startDate, endDate, "datum");
    }

    @Override
    protected String getSelectionStatementOnlyLatestRows() {
        return getSelectionStatement(LocalDate.now(), LocalDate.now());
        //        return "SELECT WL.* FROM watch_list WL RIGHT OUTER JOIN(select * from `"+CourseTableManager.TABLE_NAME+"` WP inner join ( select isin as isin_kd, max(datum) as MaxDate from `"+CourseTableManager.TABLE_NAME+"` group by isin) WPL on WP.isin = WPL.isin_kd and WP.datum = WPL.MaxDate ) KD ON WP.isin = KD.isin";
    }
}
