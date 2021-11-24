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

    // only for currency exchange correlation
    @Column(name = "wsCurrencyName", columnDefinition = "TEXT")
    private String _wsCurrencyName;
    @Transient
    private final SimpleStringProperty wsCurrencyName = new SimpleStringProperty();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteElementId", referencedColumnName = "id")
    private WebsiteElement websiteElement;

    @Transient
    private boolean isChanged = false;

    public ElementDescCorrelation() {
        initListener();
    }

    public ElementDescCorrelation(ElementSelection elementSelection, WebsiteElement websiteElement) {
        this.elementSelection = elementSelection;
        this.websiteElement = websiteElement;

        // initial state is to show the same values as in the db
        this._wsDescription = elementSelection.getDescription();
        this._wsCurrencyName = elementSelection.getDescription(); // also set for course/stock but it's useless
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

    public void setWsDescription(String wsDescription) {
        this.wsDescription.set(wsDescription);
    }

    public String getWsIsin() {
        return wsIsin.get();
    }

    public SimpleStringProperty wsIsinProperty() {
        return wsIsin;
    }

    public void setWsIsin(String wsIsin) {
        this.wsIsin.set(wsIsin);
    }

    public String getWsWkn() {
        return wsWkn.get();
    }

    public SimpleStringProperty wsWknProperty() {
        return wsWkn;
    }

    public void setWsWkn(String wsWkn) {
        this.wsWkn.set(wsWkn);
    }

    public String getWsCurrencyName() {
        return wsCurrencyName.get();
    }

    public SimpleStringProperty wsCurrencyNameProperty() {
        return wsCurrencyName;
    }

    public void setWsCurrencyName(String wsCurrencyName) {
        this.wsCurrencyName.set(wsCurrencyName);
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
        wsCurrencyName.set(_wsCurrencyName);
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
        return Objects.equals(elementSelection, that.elementSelection) && Objects.equals(_wsDescription,that._wsDescription) && Objects.equals(_wsIsin,that._wsIsin) && Objects.equals(_wsWkn,that._wsWkn) && Objects.equals(_wsCurrencyName, that._wsCurrencyName);
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
        wsCurrencyName.addListener((o, ov, nv ) -> {
            isChanged = true;
            _wsCurrencyName = nv.trim();
        });
        wsWkn.addListener((o, ov, nv) -> {
            isChanged = true;
            _wsWkn = nv.trim();
        });
    }
}
