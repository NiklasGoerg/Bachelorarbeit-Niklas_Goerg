package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementCorrelationRepository extends JpaRepository<ElementCorrelation, Integer> {
}
