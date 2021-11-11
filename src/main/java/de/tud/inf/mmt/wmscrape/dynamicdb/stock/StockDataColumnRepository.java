package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDataColumnRepository extends DynamicDbRepository<StockDataDbTableColumn, Integer> {
}
