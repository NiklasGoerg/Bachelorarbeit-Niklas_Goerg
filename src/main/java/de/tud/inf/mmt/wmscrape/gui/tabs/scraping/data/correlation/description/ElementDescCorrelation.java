package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class  ElementDescCorrelation {

    @Id
    private int id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private ElementSelection elementSelection;

    @Column(name = "wsDescription", columnDefinition = "TEXT")
    private String _wsDescription;
    @Transient
    private final SimpleStringProperty wsDescription = new SimpleStringProperty();

    // only for stock/course
    @Column(name = "wsIsin", columnDefinition = "TEXT")
    private String _wsIsin;
    @Transient
    private final SimpleStringProperty wsIsin = new SimpleStringProperty();

    @Column(name = "wsWkn", columnDefinition = "TEXT")
    private String _wsWkn;
    @Transient
    private final SimpleStringProperty wsWkn = new SimpleStringProperty();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteElementId", referencedColumnName = "id")
    private WebsiteElement websiteElement;

    @Transient
    private boolean isChanged = false;

    public ElementDescCorrelation() {}

    public ElementDescCorrelation(ElementSelection elementSelection, WebsiteElement websiteElement) {
        this.elementSelection = elementSelection;
        this.websiteElement = websiteElement;

        // initial state is to show the same values as in the db
        this._wsDescription = elementSelection.getDescription();
        this._wsIsin = elementSelection.getIsin();
        this._wsWkn = elementSelection.getWkn();
        setPropertiesFromPersistence();
        initListener();
    }

    public int getId() {
        return id;
    }

    public ElementSelection getElementSelection() {
        return elementSelection;
    }

    public void setElementSelection(ElementSelection elementSelection) {
        this.elementSelection = elementSelection;
    }

    public String getWsDescription() {
        return wsDescription.get();
    }

    public SimpleStringProperty wsDescriptionProperty() {
        return wsDescription;
    }

    public String getWsIsin() {
        return wsIsin.get();
    }

    public SimpleStringProperty wsIsinProperty() {
        return wsIsin;
    }

    public String getWsWkn() {
        return wsWkn.get();
    }

    public SimpleStringProperty wsWknProperty() {
        return wsWkn;
    }

    public WebsiteElement getWebsiteElement() {
        return websiteElement;
    }

    public void setWebsiteElement(WebsiteElement websiteElement) {
        this.websiteElement = websiteElement;
    }

    @PostLoad
    private void setPropertiesFromPersistence() {
        wsDescription.set(_wsDescription);
        wsIsin.set(_wsIsin);
        wsWkn.set(_wsWkn);
        initListener();
    }

    public boolean isChanged() {
        return isChanged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElementDescCorrelation)) return false;
        ElementDescCorrelation that;
        that = (ElementDescCorrelation) o;
        return Objects.equals(elementSelection, that.elementSelection) && Objects.equals(_wsDescription,that._wsDescription) && Objects.equals(_wsIsin,that._wsIsin) && Objects.equals(_wsWkn,that._wsWkn);
    }

    private void initListener() {
        wsDescription.addListener((o, ov, nv) -> {
            isChanged = true;
            _wsDescription = nv.trim();
        });
        wsIsin.addListener((o, ov, nv ) -> {
            isChanged = true;
            _wsIsin = nv.trim();
        });
        wsWkn.addListener((o, ov, nv) -> {
            isChanged = true;
            _wsWkn = nv.trim();
        });
    }
}
