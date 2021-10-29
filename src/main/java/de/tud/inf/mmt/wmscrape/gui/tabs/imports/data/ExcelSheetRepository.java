package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcelSheetRepository extends JpaRepository<ExcelSheet, Integer> {
}
