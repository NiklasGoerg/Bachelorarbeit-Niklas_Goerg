package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;

import javax.persistence.Entity;

@Entity
public class CourseDataDbTableColumn extends DbTableColumn {

        public CourseDataDbTableColumn() {}

        public CourseDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
        }
}
