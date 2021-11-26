package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StockDataManager extends StockAndCourseManager {

    @Autowired
    StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    StockDataDbManager stockDataDbManager;

    @Override
    protected void setColumnRepositoryAndManager(){
        dynamicDbRepository = stockDataColumnRepository;
        dynamicDbManger = stockDataDbManager;
    }
}
