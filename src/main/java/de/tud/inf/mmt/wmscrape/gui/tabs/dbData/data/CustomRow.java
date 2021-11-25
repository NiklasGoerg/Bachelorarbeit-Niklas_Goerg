package de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CustomRow {

    private final SimpleBooleanProperty isChanged =  new SimpleBooleanProperty(false);
    private final Set<CustomCell> changedCells = new HashSet<>();
    private final HashMap<String, CustomCell> cells = new HashMap<>();

    public HashMap<String, CustomCell> getCells() {
        return cells;
    }

    public void addCell(String colName, CustomCell cell) {
        cells.put(colName, cell);

        cell.isChangedProperty().addListener((o,ov,nv) -> {
            if(nv != null && nv)
                changedCells.add(cell);
                isChanged.set(true);
        });
    }

    public Set<CustomCell> getChangedCells() {
        return changedCells;
    }

    public SimpleBooleanProperty isChangedProperty() {
        return isChanged;
    }
}
