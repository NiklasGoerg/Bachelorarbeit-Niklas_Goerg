package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CourseDataDbTableColumn extends DbTableColumn {
    public CourseDataDbTableColumn() {}

    public CourseDataDbTableColumn(String name, ColumnDatatype columnDatatype) {
            super(name, columnDatatype);
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "elementIdentCorrelationId")
    private List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();


    public List<ElementIdentCorrelation> getElementIdentCorrelations() {
        return elementIdentCorrelations;
    }

    public void setElementIdentCorrelations(List<ElementIdentCorrelation> elementIdentCorrelations) {
        this.elementIdentCorrelations = elementIdentCorrelations;
    }
}