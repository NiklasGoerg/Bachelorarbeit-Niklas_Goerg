package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "excel_konfiguration")
public class ExcelSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(fetch= FetchType.LAZY, mappedBy ="excelSheet",  orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String path;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(name = "title_row")
    private int titleRow = 1;

    @Column(name = "selection_col_title")
    private String selectionColTitle;

    @Column(name = "depot_col_title")
    private String depotColTitle;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public ExcelSheet() {}

    public ExcelSheet(String description) { this.description = description; }

    public int getId() { return id; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTitleRow() {
        return titleRow;
    }

    public void setTitleRow(int titleRow) {
        this.titleRow = titleRow;
    }

    public String getSelectionColTitle() {
        return selectionColTitle;
    }

    public void setSelectionColTitle(String selectionColTitle) {
        this.selectionColTitle = selectionColTitle;
    }

    public String getDepotColTitle() {
        return depotColTitle;
    }

    public void setDepotColTitle(String depotColTitle) {
        this.depotColTitle = depotColTitle;
    }

    /**
     * used to display the description when inserting these objects into the javafx selection table
     * @return the description of the configuration
     */
    @Override
    public String toString() {
        return this.description;
    }
}
