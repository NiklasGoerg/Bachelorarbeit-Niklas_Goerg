package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseDataColumnRepository  extends DynamicDbRepository<CourseDataDbTableColumn, Integer> {
}
