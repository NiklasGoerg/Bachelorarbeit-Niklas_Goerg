package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockDataColumnRepository extends JpaRepository<StockDataTableColumn, Integer> {
//    Integer deleteByName(String name);
//    List<StockDataTableColumn> findAllByName(String name);
//    Optional<StockDataTableColumn> findByName(String name);
    List<StockDataTableColumn> findAllByName(String name);
}
