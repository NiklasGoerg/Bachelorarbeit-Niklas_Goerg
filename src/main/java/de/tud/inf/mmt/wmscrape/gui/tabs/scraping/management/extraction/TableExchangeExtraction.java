package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TableExchangeExtraction extends TableExtraction {

    public TableExchangeExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier) {
        String dbColName = carrier.getDbColName();

        String sql = "INSERT INTO `"+carrier.getDbTableName()+"` (`" + dbColName + "`, datum) VALUES(?,?) ON DUPLICATE KEY UPDATE `" +
                dbColName + "`=VALUES(`" + dbColName + "`);";

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
        // selection description == column name in exchanges
        if(selection == null) return carrier;

        carrier.setDbColName(selection.getDescription());
        carrier.setDbTableName(correlation.getDbTableName());
        return carrier;
    }

    @Override
    protected void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection) {
        // nothing to do because the date is the only key and does not change
    }

    @Override
    protected boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations) {

        for(var corr : correlations) {
            if(corr.getDbColName().equals("name") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
        }

        log("ERR:\t\tWechselkursname nicht angegeben für "+element.getInformationUrl());
        return false;
    }

    protected boolean matches(ElementDescCorrelation correlation, Map<String, InformationCarrier> carrierMap) {
        return compare(carrierMap.getOrDefault("name", null), correlation.getWsDescription());
    }

    @Override
    protected void correctCarrierValues(Map<String, InformationCarrier> carrierMap, ElementSelection selection) {
        // todo only set db col for "kurs"
        carrierMap.get("kurs").setDbColName(selection.getDescription());
        carrierMap.get("name").setDbColName(null);
//
//        for(InformationCarrier carrier : carrierMap.values()) {
//            carrier.setDbColName(selection.getDescription());
//        }
    }


    // todo
    @Override
    protected boolean prepareCarrierAndStatements(Task<Void> task, WebsiteElement websiteElement, Map<String, InformationCarrier> preparedCarrierMap) {

        for(var correlation : websiteElement.getElementIdentCorrelations()) {
            // create an information carrier with the basic information for name, kurs


            if(correlation.getDbColName().equals("name")) {
                for (var selection : websiteElement.getElementSelections()) {
                    //if(task.isCancelled()) return true;


                    var informationCarrier = prepareCarrier(correlation, selection);

                    // selection description == column name in exchanges
                    preparedCarrierMap.put(correlation.getDbColName(), informationCarrier);

                    // create a sql statement with the basic information
                    // row names stay the same
                    var statement = prepareStatement(connection, informationCarrier);
                    if (statement != null) {
                        preparedStatements.put(selection.getDescription(), statement);
                    }
                }
            } //else {
                var informationCarrier = prepareCarrier(correlation, null);
                preparedCarrierMap.put(correlation.getDbColName(), informationCarrier);
            //}
        }


        return false;
    }
}
