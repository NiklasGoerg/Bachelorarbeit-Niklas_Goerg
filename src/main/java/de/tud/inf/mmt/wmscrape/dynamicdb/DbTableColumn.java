package de.tud.inf.mmt.wmscrape.dynamicdb;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ColType")
public abstract class DbTableColumn {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ColumnDatatype columnDatatype;

    public DbTableColumn() {}

    public DbTableColumn(String name, ColumnDatatype columnDatatype) {
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

    public abstract String getTableName();

    @Override
    public String toString() {
        return name;
    }
}
