package de.tud.inf.mmt.wmscrape.gui.tabs.dbData.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class CustomCell {

    private final DbTableColumn column;
    private final SimpleStringProperty textData = new SimpleStringProperty();
    private final SimpleBooleanProperty isChanged = new SimpleBooleanProperty(false);

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

    private void cleanInput(String o, String n) {
        switch (column.getColumnDatatype()) {
            case TEXT -> textData.set(n.trim());
            case DOUBLE -> textData.set(n.replaceAll(",",".").replaceAll("[^0-9.]",""));
            case INTEGER -> textData.set(n.replaceAll("[^0-9]",""));
            case DATE -> {
                String[] split = n.replaceAll("[^0-9\\-]","").split("-");
                if(!n.trim().matches("^\\d{4}-\\d{2}-\\d{2}$")
                        || split.length < 3
                        || Integer.parseInt(split[1]) > 12
                        || Integer.parseInt(split[2]) > 31) {
                    textData.set(o);
                } else textData.set(n.trim());
            }
        }

    }
}
