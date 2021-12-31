package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * used for the data-tab data visualization to allow different
 * datatypes inside on table including validation and change notification
 */
public class CustomCell {

    private final DbTableColumn column;
    private final SimpleStringProperty textData = new SimpleStringProperty();
    private final SimpleBooleanProperty isChanged = new SimpleBooleanProperty(false);

    /**
     * adds a listener to the data, that notifies the cell containing row and flags itself and the row as modified
     *
     * @param column the database column which the data corresponds to
     * @param textData the database data as text
     */
    public CustomCell(DbTableColumn column, String textData) {
        this.column = column;
        this.textData.set(textData);

        this.textData.addListener((o,ov,nv) -> {
            if (nv != null) {
                isChanged.set(true);
                cleanInput(ov, nv);
            }
        });
    }

    public String getTextData() {
        return textData.get();
    }

    public SimpleStringProperty textDataProperty() {
        return textData;
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
     * @param oldV the old value inside the cell before editing
     * @param newV the new value after editing
     */
    private void cleanInput(String oldV, String newV) {
        switch (column.getColumnDatatype()) {
            case TEXT -> textData.set(newV.trim());

            case DOUBLE -> {
                String d = newV.replaceAll(",",".").replaceAll("[^0-9.+-]","");
                if(!d.matches("^([0-9]+(\\.[0-9]+)?|[+-][0-9]+(\\.[0-9]+)?)$")) {
                    textData.set(oldV);
                } else textData.set(d);
            }

            case INTEGER -> {
                String i = newV.replaceAll("[^0-9+-]","");
                if(!i.matches("^([0-9]+|[+-][0-9]+)$")) {
                    textData.set(oldV);
                } else textData.set(i);
            }

            case DATE -> {
                String[] split = newV.replaceAll("[^0-9\\-]","").split("-");
                if(!newV.trim().matches("^\\d{4}-\\d{2}-\\d{2}$")
                        || split.length < 3
                        || Integer.parseInt(split[1]) > 12
                        || Integer.parseInt(split[2]) > 31) {
                    textData.set(oldV);
                } else textData.set(newV.trim());
            }
        }

    }
}
