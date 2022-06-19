package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * class which represents the rows of the parameter selection table
 * in the stock parameter sub-tab
 */
public class ParameterSelection {
    private String parameter;
    private String colType;
    private ColumnDatatype dataType;
    private SimpleBooleanProperty isSelected;

    public ParameterSelection(String parameter, String colType, ColumnDatatype dataType, boolean isSelected) {
        this.parameter = parameter;
        this.colType = colType;
        this.dataType = dataType;
        this.isSelected = new SimpleBooleanProperty(isSelected);
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String isin) {
        this.parameter = isin;
    }

    public SimpleBooleanProperty isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = new SimpleBooleanProperty(selected);
    }

    public ColumnDatatype getDataType() {
        return dataType;
    }

    public void setDataType(ColumnDatatype dataType) {
        this.dataType = dataType;
    }

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }
}
