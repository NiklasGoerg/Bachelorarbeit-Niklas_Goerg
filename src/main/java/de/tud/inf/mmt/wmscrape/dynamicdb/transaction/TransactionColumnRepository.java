package de.tud.inf.mmt.wmscrape.dynamicdb.transaction;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionColumnRepository extends DbTableColumnRepository<TransactionColumn, Integer> {
}
