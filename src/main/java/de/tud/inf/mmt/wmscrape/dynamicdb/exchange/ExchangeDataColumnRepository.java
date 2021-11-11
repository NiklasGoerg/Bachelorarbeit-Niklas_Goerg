package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeDataColumnRepository extends DynamicDbRepository<ExchangeDataDbTableColumn, Integer> {
}
