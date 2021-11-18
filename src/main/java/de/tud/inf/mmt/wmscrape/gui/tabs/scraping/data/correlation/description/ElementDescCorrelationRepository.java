package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementDescCorrelationRepository extends JpaRepository<ElementDescCorrelation, Integer> {
}
