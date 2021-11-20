package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class TableCourseExtraction extends TableExtraction {

    protected TableCourseExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier) {
        return null;
    }

    @Override
    protected InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection) {
        return null;
    }

    @Override
    protected boolean validIdentCorrelations(List<ElementIdentCorrelation> elementIdentCorrelations) {

        //
//    List<ElementIdentCorrelation> identCorrelations = element.getElementIdentCorrelations();
//    var correlationDbColMap = correlationColMap(identCorrelations);
//
//        if(correlationDbColMap.size() == 0 || !correlationDbColMap.containsKey("isin")) {
//        log("FEHLER: Keine Isin Identifikation angegeben für "+element.getInformationUrl());
//        return;
//    }

        return false;
    }

    @Override
    protected void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection) {
        // add/update the sql statement information
        // e.g. setting the isin or exchange name
        // reset data to null
    }
}
