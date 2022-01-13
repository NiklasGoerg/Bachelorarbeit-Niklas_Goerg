package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.util.Objects;

/**
 * saves the correlation between some data on a website which will be extracted
 * and some key information inside the database which will be used to make a connection while scraping website tables.
 *
 * there is a one to one relation between an {@link de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection}
 * because one correlation describes one selection
 */
@Entity
@Table(name = "webseiten_element_abbildung")
public class  ElementDescCorrelation {

    @Id
    private int id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", referencedColumnName = "id", updatable = false, nullable = false, insertable = false)
    private ElementSelection elementSelection;

    @Column(name = "ws_description", columnDefinition = "TEXT")
    private String _wsDescription;

    // only for stock/course
    @Column(name = "ws_isin", columnDefinition = "TEXT")
    private String _wsIsin;

    @Column(name = "ws_wkn", columnDefinition = "TEXT")
    private String _wsWkn;

    @Transient
    private final SimpleStringProperty wsDescription = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty wsIsin = new SimpleStringProperty();
    @Transient
    private final SimpleStringProperty wsWkn = new SimpleStringProperty();
    @Transient
    private boolean isChanged = false;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public ElementDescCorrelation() {}

    public ElementDescCorrelation(ElementSelection elementSelection) {
        this.elementSelection = elementSelection;

        // initial state is to show the same values as in the db
        this._wsDescription = elementSelection.getDescription();
        this._wsIsin = elementSelection.getIsin();
        this._wsWkn = elementSelection.getWkn();
        setPropertiesFromPersistence();
        initListener();
    }

    public ElementSelection getElementSelection() {
        return elementSelection;
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

    /**
     * called after entity creation by hibernate (loading from the database)
     * updates the property values to those from the database
     */
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
        if (!(o instanceof ElementDescCorrelation)) return false;
        ElementDescCorrelation that;
        that = (ElementDescCorrelation) o;
        return Objects.equals(elementSelection, that.elementSelection) && Objects.equals(_wsDescription,that._wsDescription) && Objects.equals(_wsIsin,that._wsIsin) && Objects.equals(_wsWkn,that._wsWkn);
    }

    /**
     * allows using properties which can't be stored by hibernate.
     * when a property changes the filed inside the entity changes which can be stored as usual
     */
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
