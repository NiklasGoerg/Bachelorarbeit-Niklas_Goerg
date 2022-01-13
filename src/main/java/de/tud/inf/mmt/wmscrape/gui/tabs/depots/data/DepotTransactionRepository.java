package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepotTransactionRepository extends JpaRepository<DepotTransaction, Integer> {
}
