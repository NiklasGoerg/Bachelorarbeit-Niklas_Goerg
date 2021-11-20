package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableCourseOrStockExtraction extends TableExtraction {

    protected TableCourseOrStockExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
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
        carrier.setIsin(selection.getIsin());
        return carrier;
    }

    @Override
    protected boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations) {

        List<String> colNames =
                correlations.stream()
                        .map(ElementIdentCorrelation::getDbColName)
                        .collect(Collectors.toList());

        if(colNames.contains("isin") || colNames.contains("wkn") || colNames.contains("name")) {
            return true;
        }

        log("FEHLER: Weder ISIN, WKN noch Namen angegeben für "+element.getInformationUrl());
        return false;
    }

    protected boolean matches(List<ElementDescCorrelation> descCorrelations, Map<String, InformationCarrier> carrierMap) {

        String extractedIsin = carrierMap.getOrDefault("isin", null).getExtractedData();
        String extractedWkn = carrierMap.getOrDefault("wkw", null).getExtractedData();
        String extractedDesc = carrierMap.getOrDefault("name", null).getExtractedData();

        // correct in e.g. correctIsin means the mapped value fits
        // like ABCD in db has a mapping to 1234 and the extracted value has 1234
        // this would be a match

        // check matching isin
        if(extractedIsin != null && extractedIsin.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctIsin = descCorrelation.getWsIsin();
                if(compare(extractedIsin, correctIsin)) {
                    return true;
                }
            }
        }

        // check matching wkn
        if(extractedWkn != null && extractedWkn.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctWkn = descCorrelation.getWsWkn();
                if(compare(extractedWkn, correctWkn)) {
                    return true;
                }
            }
        }

        // check matching description
        if(extractedDesc != null && extractedDesc.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctDescription = descCorrelation.getWsDescription();
                if(compare(extractedDesc, correctDescription)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void setCorrectValuesFromSelection(Map<String, InformationCarrier> carrierMap, ElementSelection selection) {
        var isinCarrier = carrierMap.getOrDefault("isin", new InformationCarrier(date, ColumnDatatype.TEXT));
        var wknCarrier = carrierMap.getOrDefault("wkw", new InformationCarrier(date, ColumnDatatype.TEXT));
        var descCarrier = carrierMap.getOrDefault("name", new InformationCarrier(date, ColumnDatatype.TEXT));

        isinCarrier.setExtractedData(selection.getIsin());
        wknCarrier.setExtractedData(selection.getWkn());
        descCarrier.setExtractedData(selection.getDescription());

        carrierMap.put("isin", isinCarrier);
        carrierMap.put("wkn", wknCarrier);
        carrierMap.put("name", descCarrier);
    }

}
