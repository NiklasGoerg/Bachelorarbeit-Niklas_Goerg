package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to fetch column entities from the database
 */
@Repository
public interface ExchangeColumnRepository extends DbTableColumnRepository<ExchangeColumn, Integer> {
}
