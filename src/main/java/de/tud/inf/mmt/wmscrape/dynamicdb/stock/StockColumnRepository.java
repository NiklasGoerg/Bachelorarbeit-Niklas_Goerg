package de.tud.inf.mmt.wmscrape.dynamicdb.stock;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockColumnRepository extends DbTableColumnRepository<StockColumn, Integer> {
}
