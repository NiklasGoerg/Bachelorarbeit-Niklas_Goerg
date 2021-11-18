package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class WebsiteElement {
    @Id
    @GeneratedValue
    private int id;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteId", referencedColumnName = "id")
    private Website website;

    @Column(columnDefinition = "TEXT")
    private String informationUrl;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    private MultiplicityType multiplicityType;

    //only for table scraping
    @Column(columnDefinition = "TEXT")
    private String tableIdent;

    @Enumerated(EnumType.STRING)
    private IdentType tableIdenType = IdentType.ID;

    @OneToMany(mappedBy = "websiteElement", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementSelection> elementSelections = new ArrayList<>();

    @OneToMany(mappedBy = "websiteElement", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();

    @OneToMany(mappedBy = "websiteElement", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementDescCorrelation> elementDescCorrelations = new ArrayList<>();


    public WebsiteElement() {}

    public WebsiteElement(String description, ContentType contentType, MultiplicityType multiplicityType) {
        this.description = description;
        this.contentType = contentType;
        this.multiplicityType = multiplicityType;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getInformationUrl() {
        return informationUrl;
    }

    public void setInformationUrl(String informationUrl) {
        this.informationUrl = informationUrl;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public MultiplicityType getMultiplicityType() {
        return multiplicityType;
    }

    public String getTableIdent() {
        return tableIdent;
    }

    public void setTableIdent(String tableIdent) {
        this.tableIdent = tableIdent;
    }

    public IdentType getTableIdenType() {
        return tableIdenType;
    }

    public void setTableIdenType(IdentType tableIdenType) {
        this.tableIdenType = tableIdenType;
    }

    public List<ElementSelection> getElementSelections() {
        return elementSelections;
    }

    public void setElementSelections(List<ElementSelection> elementSelections) {
        this.elementSelections = elementSelections;
    }

    public List<ElementIdentCorrelation> getElementIdentCorrelations() {
        return elementIdentCorrelations;
    }

    public void setElementCorrelations(List<ElementIdentCorrelation> elementIdentCorrelations) {
        this.elementIdentCorrelations = elementIdentCorrelations;
    }

    public List<ElementDescCorrelation> getElementDescCorrelations() {
        return elementDescCorrelations;
    }

    public void removeElementDescCorrelation(ElementDescCorrelation correlation) {
        elementDescCorrelations.remove(correlation);
    }

    public void removeElementSelection(ElementSelection elementSelection) {
        elementSelections.remove(elementSelection);
    }

    @Override
    public String toString() {
        return this.description;
    }
}
