package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("C")
public class CourseDataDbTableColumn extends DbTableColumn {

    public CourseDataDbTableColumn() {}

    public CourseDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
    }


    @OneToMany(mappedBy = "courseDataDbTableColumn", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();

    public List<ElementIdentCorrelation> getElementIdentCorrelations() {
        return elementIdentCorrelations;
    }

    public void setElementIdentCorrelations(List<ElementIdentCorrelation> elementIdentCorrelations) {
        this.elementIdentCorrelations = elementIdentCorrelations;
    }

    @Override
    public String getTableName() {
        return CourseDataDbManager.TABLE_NAME;
    }
}
