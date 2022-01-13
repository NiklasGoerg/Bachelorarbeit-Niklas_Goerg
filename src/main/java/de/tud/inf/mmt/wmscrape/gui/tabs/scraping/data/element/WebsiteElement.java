package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebRepresentation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * holds the information for one website element configuration of different types.
 *
 * can exist without a website configuration attached to it
 */
@Entity
@Table(name = "webseiten_element")
public class WebsiteElement extends WebRepresentation<WebRepresentation<?>> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // set to null if website config is removed -> no cascading delete
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "website_id", referencedColumnName = "id")
    private Website website;

    @OneToMany(mappedBy = "websiteElement", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementSelection> elementSelections = new ArrayList<>();

    @OneToMany(mappedBy = "websiteElement", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private String description;

    @Column(columnDefinition = "TEXT", name = "information_url")
    private String informationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, updatable = false)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "multiplicity_type", nullable = false, updatable = false)
    private MultiplicityType multiplicityType;

    //only for table scraping
    @Column(columnDefinition = "TEXT", name = "table_ident")
    private String tableIdent;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_iden_type", nullable = false)
    private IdentType tableIdenType = IdentType.ID;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
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

    /**
     * used for the representation inside the scraping menu with a selection tree
     * @return website elements have no children only the {@link de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website} configurations have
     */
    @Override
    public List<WebRepresentation<?>> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return this.description;
    }

    /**
     * due to the fact that hibernate creates proxies (subclasses of the actual entities) one has to use "instanceof" to compare
     * objects. normally checking of equality can cause unexpected results.
     * lazy loaded fields are omitted because one can not know if a session is still attached.
     *
     * @param o the object to compare to
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebsiteElement that)) return false;
        return id == that.id && Objects.equals(description, that.description) && Objects.equals(informationUrl, that.informationUrl) && contentType == that.contentType && multiplicityType == that.multiplicityType && Objects.equals(tableIdent, that.tableIdent) && tableIdenType == that.tableIdenType;
    }

    /**
     * used for saving the selected elements inside the selection tree in the scraping menu as hash values
     * extra value 1 to differentiate between websites
     * @return the hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, description, 1);
    }
}
