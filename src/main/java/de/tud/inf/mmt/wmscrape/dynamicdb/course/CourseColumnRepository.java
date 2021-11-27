package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseColumnRepository extends DbTableColumnRepository<CourseColumn, Integer> {
}
