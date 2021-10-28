package de.tud.inf.mmt.wmscrape.gui.tabs.imports.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ExcelSheet {
    @Id
    @GeneratedValue
    private int id;
    private String description;
    private String path;
    private String password;
    private int titleRow;
    private String selectionColTitle;

    @OneToMany(fetch= FetchType.LAZY, mappedBy ="excelSheet",  orphanRemoval = true)
    private List<ExcelCorrelation> excelCorrelations = new ArrayList<>();

    public int getId() {
        return id;
    }

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

    public List<ExcelCorrelation> getExcelCorrelations() {
        return excelCorrelations;
    }

    public void addExcelCorrelation(ExcelCorrelation excelCorrelation) {
        this.excelCorrelations.add(excelCorrelation);
    }
}
