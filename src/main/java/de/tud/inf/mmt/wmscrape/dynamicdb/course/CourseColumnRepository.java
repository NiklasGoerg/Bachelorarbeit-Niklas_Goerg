package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to fetch column entities from the database
 */
@Repository
public interface CourseColumnRepository extends DbTableColumnRepository<CourseColumn, Integer> {
}
