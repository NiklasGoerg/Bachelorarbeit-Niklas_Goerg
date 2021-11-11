package de.tud.inf.mmt.wmscrape.gui.tabs.data.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDataColumnRepository extends DynamicDbRepository<StockDataTableColumn, Integer> {
}
