package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class ElementDescCorrelation {

    @Id
    private int id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id")
    private ElementSelection elementSelection;

    @Column(name = "wsDescription")
    private String _wsDescription;
    @Transient
    private final SimpleStringProperty wsDescription = new SimpleStringProperty();
    @Column(name = "wsIsin")
    private String _wsIsin;
    @Transient
    private final SimpleStringProperty wsIsin = new SimpleStringProperty();

    // only for currency exchange correlation
    @Column(name = "wsCurrencyName")
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
        this();
        this.elementSelection = elementSelection;
        this.websiteElement = websiteElement;
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
        return Objects.equals(elementSelection, that.elementSelection) && Objects.equals(_wsDescription,that._wsDescription) && Objects.equals(_wsIsin,that._wsIsin) && Objects.equals(_wsCurrencyName, that._wsCurrencyName);
    }

    private void initListener() {
        wsDescription.addListener((o, ov, nv) -> {
            isChanged = true;
            _wsDescription = nv;
        });
        wsIsin.addListener((o, ov, nv ) -> {
            isChanged = true;
            _wsIsin = nv;
        });
        wsCurrencyName.addListener((o, ov, nv ) -> {
            isChanged = true;
            _wsCurrencyName = nv;
        });
    }
}
