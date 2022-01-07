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

    // the MySQL datatype
    @Enumerated(EnumType.STRING)
    @Column(name = "column_datatype", nullable = false, updatable = false)
    private ColumnDatatype columnDatatype;

    // the datatype used for representational purposes
    @Enumerated(EnumType.STRING)
    @Column(name = "column_visual_datatype", nullable = false, updatable = false)
    private VisualDatatype visualDatatype;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public DbTableColumn() {}

    public DbTableColumn(String name, ColumnDatatype columnDatatype) {
        this.name = name;
        this.columnDatatype = columnDatatype;
        this.visualDatatype = DbTableManger.translateVisualDatatype(columnDatatype);
    }

    public DbTableColumn(String name, VisualDatatype visualDatatype) {
        this.name = name;
        this.columnDatatype = DbTableManger.translateDataType(visualDatatype);
        this.visualDatatype = visualDatatype;
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

    public VisualDatatype getColumnVisualDatatype() {
        return visualDatatype;
    }

    public abstract String getTableName();

    @Override
    public String toString() {
        return name;
    }
}
