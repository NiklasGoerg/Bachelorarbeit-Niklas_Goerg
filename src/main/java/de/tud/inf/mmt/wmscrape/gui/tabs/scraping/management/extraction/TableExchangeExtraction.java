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
        // the carrier stays as is if created for the "kurs" identCorrelation
        if(selection == null) return carrier;

        // the column/table names have to be set here only for the statement creation
        // the flow is prepareCarrierAndStatements -> extendCarrier -> prepareStatement
        // prepareStatement uses the dbColName to create the statement for each selection whose description is
        // actually a column name
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
        // the column name is the description of the selection
        carrierMap.get("kurs").setDbColName(selection.getDescription());

        // by setting the column to null. there is no statement found inside the "setStatementExtractedData" function
        // therefore no value will be stored for "name"
        carrierMap.get("name").setDbColName(null);
    }


    @Override
    protected boolean prepareCarrierAndStatements(Task<Void> task, WebsiteElement websiteElement, Map<String, InformationCarrier> preparedCarrierMap) {
        // this one needs special handling as there are no entities attached to the selections. the selections are the
        // columns of the table.
        // the identCorrelations are static and only two (name, kurs) exist

        for(var correlation : websiteElement.getElementIdentCorrelations()) {
            if(correlation.getDbColName().equals("name")) {
                for (var selection : websiteElement.getElementSelections()) {
                    if(task.isCancelled()) return true;


                    // prepares the carrier and extends it with the column name which is the description from
                    // the selection as the selection is the database column
                    // the carrier is here actually only used to create sql statements without modifying
                    // the creation process
                    var informationCarrier = prepareCarrier(correlation, selection);

                    // this step could be done only once as the currently set column name does not matter
                    // the column name is updated when needed in the "correctCarrierValues" function
                    preparedCarrierMap.put(correlation.getDbColName(), informationCarrier);

                    // saves the statement for each selected column name like
                    // (colName, date) -> ("data from matching row", "today")
                    var statement = prepareStatement(connection, informationCarrier);
                    if (statement != null) preparedStatements.put(selection.getDescription(), statement);
                }
            } else {
                // create the carrier for "kurs"
                // this one doesn't need the selection to retrieve the column name
                // no statements are created fot this as there is no need to store it in the database
                preparedCarrierMap.put(correlation.getDbColName(), prepareCarrier(correlation, null));
            }
        }

        return false;
    }
}
