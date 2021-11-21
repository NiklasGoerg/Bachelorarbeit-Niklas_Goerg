package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TableCourseOrStockExtraction extends TableExtraction {

    public TableCourseOrStockExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier) {
        String dbColName = carrier.getDbColName();

        String sql = "INSERT INTO "+carrier.getDbTableName()+" ("+dbColName+", isin, datum) VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName+"=VALUES("+dbColName+");";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            // isin is set in updateStatements corresponding to the matching element
            statement.setDate(3, carrier.getDate());
            return statement;

        } catch (SQLException e) {
            handleSqlException(carrier, e);
        }
        return null;
    }

    @Override
    protected void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection) {
        String isin = selection.getIsin();

        for(PreparedStatement statement : statements.values()) {
            try {
                statement.setString(2, isin);
            } catch (SQLException e) {
                log("FEHLER: Setzen der ISIN '"+isin+"' fehlgeschlagen für "+selection.getDescription());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection) {
        carrier.setDbColName(correlation.getDbColName());
        carrier.setDbTableName(correlation.getDbTableName());
        return carrier;
    }

    @Override
    protected boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations) {

        for(var corr : correlations) {
            if(corr.getDbColName().equals("isin") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
            if(corr.getDbColName().equals("wkn") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
            if(corr.getDbColName().equals("name") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
        }

        log("FEHLER: Weder ISIN, WKN noch Namen angegeben für "+element.getInformationUrl());
        return false;
    }

    protected boolean matches(List<ElementDescCorrelation> descCorrelations, Map<String, InformationCarrier> carrierMap) {

        String extractedData;

        // correct in e.g. correctIsin means the mapped value fits
        // like ABCD in database has a mapping to 1234 and the extracted value has 1234
        // this would be a match


        var carrier = carrierMap.getOrDefault("isin", null);
        if(carrier != null) {
            extractedData = carrier.getExtractedData();

            // check matching isin
            if (extractedData != null && extractedData.length() > 0) {
                for (var corr : descCorrelations) {
                    var correctIsin = corr.getWsIsin();
                    if (compare(extractedData, correctIsin)) {
                        return notYetExtracted(corr);
                    }
                }
            }
        }


        carrier = carrierMap.getOrDefault("wkn", null);
        if(carrier != null) {
            extractedData = carrier.getExtractedData();

            // check matching wkn
            if (extractedData != null && extractedData.length() > 0) {
                for (var corr : descCorrelations) {
                    var correctWkn = corr.getWsWkn();
                    if (compare(extractedData, correctWkn)) {
                        return notYetExtracted(corr);
                    }
                }
            }
        }


        carrier = carrierMap.getOrDefault("name", null);
        if(carrier != null) {
            extractedData = carrier.getExtractedData();

            // check matching description
            if (extractedData != null && extractedData.length() > 0) {
                for (var corr : descCorrelations) {
                    var correctDescription = corr.getWsDescription();
                    if (compare(extractedData, correctDescription)) {
                        return notYetExtracted(corr);
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected void setCorrectValuesFromSelection(Map<String, InformationCarrier> carrierMap, ElementSelection selection) {
        var isinCarrier = carrierMap.getOrDefault("isin", new InformationCarrier(date, ColumnDatatype.TEXT, "isin"));
        var wknCarrier = carrierMap.getOrDefault("wkn", new InformationCarrier(date, ColumnDatatype.TEXT, "wkn"));
        var descCarrier = carrierMap.getOrDefault("name", new InformationCarrier(date, ColumnDatatype.TEXT,"name"));

        isinCarrier.setExtractedData(selection.getIsin());
        wknCarrier.setExtractedData(selection.getWkn());
        descCarrier.setExtractedData(selection.getDescription());

        // TODO necessary?
        carrierMap.put("isin", isinCarrier);
        carrierMap.put("wkn", wknCarrier);
        carrierMap.put("name", descCarrier);
    }

}
