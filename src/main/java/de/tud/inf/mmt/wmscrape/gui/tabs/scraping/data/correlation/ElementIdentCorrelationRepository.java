package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementIdentCorrelationRepository extends JpaRepository<ElementIdentCorrelation, Integer> {
}
