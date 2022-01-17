package de.tud.inf.mmt.wmscrape.dynamicdb;

/**
 * MySQL based column datatypes.
 * must be valid MySQL datatype names
 */
public enum ColumnDatatype {
    INTEGER,
    DOUBLE,
    DATE,
    TEXT,
    DATETIME // atm only an addition for the depot transaction ate column -> only necessary for comparing in the data tab.
}
