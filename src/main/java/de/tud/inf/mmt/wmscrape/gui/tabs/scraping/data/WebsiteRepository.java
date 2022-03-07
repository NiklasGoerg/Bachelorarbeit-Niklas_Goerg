package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, Integer> {
    List<Website> findAllByIsHistoricTrue();
    List<Website> findAllByIsHistoricFalse();
}
