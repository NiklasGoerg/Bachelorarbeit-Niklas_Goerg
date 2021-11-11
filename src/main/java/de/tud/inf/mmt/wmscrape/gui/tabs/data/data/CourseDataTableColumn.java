package de.tud.inf.mmt.wmscrape.gui.tabs.data.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.TableColumn;

import javax.persistence.Entity;

@Entity
public class CourseDataTableColumn extends TableColumn{

        public CourseDataTableColumn() {}

        public CourseDataTableColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
        }
}
