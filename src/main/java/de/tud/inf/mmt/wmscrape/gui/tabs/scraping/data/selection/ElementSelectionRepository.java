package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementSelectionRepository extends JpaRepository<ElementSelection, Integer> {
    /**
     * used to remove unused selection. this could be done better by not saving them in the first place.
     * @param isSelected true for an element if it has been selected inside the selection list
     */
    void deleteAllBy_selected(boolean isSelected);
}
