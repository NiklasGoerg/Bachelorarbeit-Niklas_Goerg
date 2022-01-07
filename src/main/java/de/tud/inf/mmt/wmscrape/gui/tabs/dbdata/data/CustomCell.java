package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

/**
 * used for the data-tab data visualization to allow different
 * datatypes inside on table including validation and change notification
 */
public class CustomCell {

    private final DbTableColumn column;
    private String dbData;
    private String visualizedData = null;
    private boolean skipListener = false; // stops the lister from acting on visualization changes
    private final SimpleStringProperty visualizedDataProperty = new SimpleStringProperty();
    private final SimpleBooleanProperty isChanged = new SimpleBooleanProperty(false);

    /**
     * adds a listener to the data, that notifies the cell containing row and flags itself and the row as modified
     *
     * @param column the database column which the data corresponds to
     * @param dbData the database data as text
     */
    public CustomCell(DbTableColumn column, String dbData) {
        this.column = column;

        this.dbData = dbData;
        setVisualizedData();

        // called after input commit and after the onEditCommitEvent function
        this.visualizedDataProperty.addListener((o, ov, nv) -> {
            if (nv != null && !skipListener && !nv.equals(ov)) {
                isChanged.set(true);
                visualizedDataProperty.set(visualizedData);
            }
        });
    }

    public String getDbData() {
        return dbData;
    }

    public SimpleStringProperty visualizedDataPropertyProperty() {
        return visualizedDataProperty;
    }

    public ColumnDatatype getDatatype() {
        return column.getColumnDatatype();
    }

    public String getColumnName() {
        return column.getName();
    }

    public String getTableName() {
        return column.getTableName();
    }

    public SimpleBooleanProperty isChangedProperty() {
        return isChanged;
    }

    /**
     * validates the cell based on its datatype
     *
     * @param newV the new value after editing
     */
    private void cleanInput(String newV) {
        switch (column.getColumnDatatype()) {
            case TEXT -> dbData = newV.trim();//visualizedData.set(newV.trim());

            case DOUBLE -> {
                String d = newV.replaceAll(",",".").replaceAll("[^0-9.+-]","");
                if(d.matches("^([0-9]+(\\.[0-9]+)?|[+-][0-9]+(\\.[0-9]+)?)$")) {
                    dbData = d;
                }
            }

            case INTEGER -> {
                String i = newV.replaceAll("[^0-9+-]","");
                if(i.matches("^([0-9]+|[+-][0-9]+)$")) {
                    dbData = i;
                }
            }

            case DATE -> {
                String[] split = newV.replaceAll("[^0-9\\-]","").split("-");
                if(newV.trim().matches("^\\d{4}-\\d{2}-\\d{2}$")
                        && split.length >= 3
                        && Integer.parseInt(split[1]) <= 12
                        && Integer.parseInt(split[2]) <= 31) {
                    dbData = newV.trim();
                }
            }
        }
    }


    /**
     * called when entering the cell edit mode in the table
     */
    public void onEditStartEvent() {
        skipListener = true;
        visualizedDataProperty.set(dbData);
    }

    /**
     * called when leaving the cell edit mode in the table
     */
    public void onEditCancelEvent() {
        visualizedDataProperty.set(visualizedData);
    }

    /**
     * called when committing the cell value changes
     */
    public void onEditCommitEvent(TableColumn.CellEditEvent<Object, Object> event) {
        skipListener = false;
        cleanInput((String) event.getNewValue());
        setVisualizedData();
    }

    /**
     * adds the suffix corresponding to the {@link de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype}
     * and sets it to the property
     * don't forget to change the comparator in {@link de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management.DataManager}
     */
    private void setVisualizedData() {
        if (dbData == null || dbData.isBlank()) return;

        String suffix = "";

        switch (column.getColumnVisualDatatype()) {
            case Doller -> suffix = " $";
            case Euro -> suffix = " €";
            case Prozent -> suffix = " %";
        }

        visualizedData = dbData + suffix;
        visualizedDataProperty.set(visualizedData);
    }
}
