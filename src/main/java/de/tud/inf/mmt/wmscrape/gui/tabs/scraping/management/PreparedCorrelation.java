package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;

import java.sql.Date;

public class PreparedCorrelation {

    private final String dbTableName;
    private final String dbColName;
    private final Date date;
    private final ColumnDatatype datatype;
    private final IdentType identType;
    private final String identifier;

    // stock/course
    private String isin;
    private String websiteData;

    // stock/course table
    private String wkn;


    public PreparedCorrelation(String dbTableName, String dbColName, Date date, ColumnDatatype datatype, IdentType identType, String identifier) {
        this.dbTableName = dbTableName;
        this.dbColName = dbColName;
        this.date = date;
        this.datatype = datatype;
        this.identType = identType;
        this.identifier = identifier;
    }


    public String getDbTableName() {
        return dbTableName;
    }

    public String getDbColName() {
        return dbColName;
    }

    public Date getDate() {
        return date;
    }

    public ColumnDatatype getDatatype() {
        return datatype;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getWebsiteData() {
        return websiteData;
    }

    public void setWebsiteData(String websiteData) {
        this.websiteData = websiteData;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getWkn() {
        return wkn;
    }

    public void setWkn(String wkn) {
        this.wkn = wkn;
    }
}
