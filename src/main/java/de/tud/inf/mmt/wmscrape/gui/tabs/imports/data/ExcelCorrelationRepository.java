package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExcelCorrelationRepository extends JpaRepository<ExcelCorrelation, Integer> {
    List<ExcelCorrelation> findAllByExcelSheetId(Integer id);
}
