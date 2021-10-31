package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExcelCorrelationRepository extends JpaRepository<ExcelCorrelation, Integer> {
    List<ExcelCorrelation> findAllByExcelSheetId(Integer id);
}
