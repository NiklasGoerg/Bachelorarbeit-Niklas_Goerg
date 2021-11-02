package de.tud.inf.mmt.wmscrape.gui.tabs.depots.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DepotRepository extends CrudRepository<Depot, Integer> {
    Optional<Depot> findByName(String name);
}
