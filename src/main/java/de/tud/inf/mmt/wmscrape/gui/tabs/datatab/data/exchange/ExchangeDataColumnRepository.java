package de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeDataColumnRepository extends DynamicDbRepository<ExchangeDataTableColumn, Integer> {
}
