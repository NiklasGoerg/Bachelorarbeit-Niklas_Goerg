package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends CrudRepository<Stock, String> {
    Optional<Stock> findByIsin(String isin);
}
