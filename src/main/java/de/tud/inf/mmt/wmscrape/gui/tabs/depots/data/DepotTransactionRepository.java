package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.Optional;

public interface DepotTransactionRepository extends JpaRepository<DepotTransaction, Integer> {
    Optional<DepotTransaction> findByDepotNameAndDateAndStockIsin(String depotName, Date date, String stockIsin);
}
