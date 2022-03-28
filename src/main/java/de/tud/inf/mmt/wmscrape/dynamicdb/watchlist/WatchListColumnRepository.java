package de.tud.inf.mmt.wmscrape.dynamicdb.watchlist;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to fetch column entities from the database
 */
@Repository
public interface WatchListColumnRepository extends DbTableColumnRepository<WatchListColumn, Integer> {
}
