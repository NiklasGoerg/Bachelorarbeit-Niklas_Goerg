package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * the object which is inserted into the javafx table containing all the cells for one row
 */
public class CustomRow {

    private final SimpleBooleanProperty isChanged =  new SimpleBooleanProperty(false);
    private final Set<CustomCell> changedCells = new HashSet<>();
    private final HashMap<String, CustomCell> cells = new HashMap<>();

    public HashMap<String, CustomCell> getCells() {
        return cells;
    }

    /**
     *
     * @param colName the database column name the cell corresponds to
     * @param cell the cell with the data
     */
    public void addCell(String colName, CustomCell cell) {
        cells.put(colName, cell);

        cell.isChangedProperty().addListener((o,ov,nv) -> {
            if(nv != null && nv)
                changedCells.add(cell);
                isChanged.set(true);
        });
    }

    /**
     *
     * @return the changed cells based on the notification from the single cells
     */
    public Set<CustomCell> getChangedCells() {
        return changedCells;
    }

    public SimpleBooleanProperty isChangedProperty() {
        return isChanged;
    }
}
