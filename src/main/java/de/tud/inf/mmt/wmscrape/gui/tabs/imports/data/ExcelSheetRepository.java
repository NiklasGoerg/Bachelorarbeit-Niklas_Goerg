package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelSheetRepository extends JpaRepository<ExcelSheet, Integer> {
}
