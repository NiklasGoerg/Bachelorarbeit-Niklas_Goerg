package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDataColumnRepository extends DynamicDbRepository<TransactionDataDbTableColumn, Integer> {
}
