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

public class TableExchangeExtraction extends TableExtraction {

    protected TableExchangeExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier) {
        String dbColName = carrier.getDbColName();

        String sql = "INSERT INTO "+carrier.getDbTableName()+" (" + dbColName + ", datum) VALUES(?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(2, carrier.getDate());
            return statement;

        } catch (SQLException e) {
            handleSqlException(carrier, e);
        }
        return null;
    }

    @Override
    protected InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection) {
        carrier.setDbColName(correlation.getDbColName());
        carrier.setDbTableName(correlation.getDbTableName());
        return carrier;
    }

    @Override
    protected void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection) {
        // nothing to do because the date is the only key
    }

    @Override
    protected boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations) {

        List<String> colNames =
                correlations.stream()
                        .map(ElementIdentCorrelation::getDbColName)
                        .collect(Collectors.toList());

        if(colNames.contains("bezeichnung")) {
            return true;
        }

        log("FEHLER: Kursbezeichnung nicht angegeben für "+element.getInformationUrl());
        return false;
    }

    protected boolean matches(List<ElementDescCorrelation> descCorrelations, Map<String, InformationCarrier> carrierMap) {

        String extractedDescription = carrierMap.getOrDefault("bezeichnung", null).getExtractedData();

        // check matching description like EUR
        if(extractedDescription != null && extractedDescription.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctDesc = descCorrelation.getWsCurrencyName();
                if(compare(extractedDescription, correctDesc)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void setCorrectValuesFromSelection(Map<String, InformationCarrier> carrierMap, ElementSelection selection) {
        var descCarrier = carrierMap.getOrDefault("bezeichnung", new InformationCarrier(date, ColumnDatatype.TEXT));

        descCarrier.setExtractedData(selection.getDescription());

        carrierMap.put("bezeichnung", descCarrier);
    }
}
