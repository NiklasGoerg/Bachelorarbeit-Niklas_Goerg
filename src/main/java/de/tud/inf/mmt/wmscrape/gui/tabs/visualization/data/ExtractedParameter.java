package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data;

import java.sql.Date;

public class ExtractedParameter {
    private String isin;
    private String name;
    private Date date;
    private Number parameter;
    private String parameterName;

    public ExtractedParameter(String isin, String name, Date date, Number parameter, String parameterName) {
        this.isin = isin;
        this.name = name;
        this.date = date;
        this.parameter = parameter;
        this.parameterName = parameterName;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Number getParameter() {
        return parameter;
    }

    public void setParameter(Number parameter) {
        this.parameter = parameter;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
}
