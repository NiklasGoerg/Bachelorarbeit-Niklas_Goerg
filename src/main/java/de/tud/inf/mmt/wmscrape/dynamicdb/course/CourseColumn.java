package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * column entity for courses (prices)
 */
@Entity
@DiscriminatorValue("C")
public class CourseColumn extends DbTableColumn {

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public CourseColumn() {}

    public CourseColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
    }

    @OneToMany(mappedBy = "courseColumn", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();

    @Override
    public String getTableName() {
        return CourseTableManager.TABLE_NAME;
    }
}
