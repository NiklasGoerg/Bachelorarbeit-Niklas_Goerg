package de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseDataColumnRepository  extends DynamicDbRepository<CourseDataTableColumn, Integer> {
}
