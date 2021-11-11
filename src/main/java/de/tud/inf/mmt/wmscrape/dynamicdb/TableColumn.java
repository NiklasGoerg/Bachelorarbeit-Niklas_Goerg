package de.tud.inf.mmt.wmscrape.dynamicdb;

import javax.persistence.*;

@MappedSuperclass
public abstract class TableColumn {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ColumnDatatype columnDatatype;

    public TableColumn() {}

    public TableColumn(String name, ColumnDatatype columnDatatype) {
        this.name = name;
        this.columnDatatype = columnDatatype;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnDatatype getColumnDatatype() {
        return columnDatatype;
    }

    public void setColumnDatatype(ColumnDatatype columnDatatype) {
        this.columnDatatype = columnDatatype;
    }
}
