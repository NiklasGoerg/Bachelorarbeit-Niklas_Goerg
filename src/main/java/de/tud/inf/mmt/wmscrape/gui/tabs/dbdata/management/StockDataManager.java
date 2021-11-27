package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
