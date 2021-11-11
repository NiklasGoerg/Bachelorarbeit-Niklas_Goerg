package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementSelectionRepository extends JpaRepository<ElementSelection, Integer> {

}
