package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchListRepository extends JpaRepository<WatchList, String> {
    Optional<Stock> findByIsin(String isin);
}
