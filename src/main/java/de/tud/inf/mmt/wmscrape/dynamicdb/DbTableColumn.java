package de.tud.inf.mmt.wmscrape.dynamicdb;

import javax.persistence.*;

/**
 * general column entity
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "datenbank_spalte")
@DiscriminatorColumn(name="col_type")
public abstract class DbTableColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, updatable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "column_datatype", nullable = false, updatable = false)
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

    public ColumnDatatype getColumnDatatype() {
        return columnDatatype;
    }

    public abstract String getTableName();

    @Override
    public String toString() {
        return name;
    }
}
