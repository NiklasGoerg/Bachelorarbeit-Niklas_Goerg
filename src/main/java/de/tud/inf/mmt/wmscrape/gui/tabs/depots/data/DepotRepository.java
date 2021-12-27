package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepotRepository extends JpaRepository<Depot, Integer> {
    Optional<Depot> findByName(String name);
}
