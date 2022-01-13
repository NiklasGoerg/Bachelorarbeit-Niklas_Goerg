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

public class TableCourseOrStockExtraction extends TableExtraction {

    private final static List<String> ignoreIdentCorrelationColumns = List.of("isin", "wkn", "name");

    public TableCourseOrStockExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier) {
        String dbColName = carrier.getDbColName();

        String sql = "INSERT INTO `"+carrier.getDbTableName()+"` (`"+dbColName+"`, isin, datum) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `" +
                dbColName+"`=VALUES(`"+dbColName+"`);";

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
                log("ERR:\t\tSetzen der ISIN '"+isin+"' fehlgeschlagen für "+selection.getDescription());
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
        // check that at least one of them is not deactivated
        for(var corr : correlations) {
            if(corr.getDbColName().equals("isin") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
            if(corr.getDbColName().equals("wkn") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
            if(corr.getDbColName().equals("name") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
        }

        log("ERR:\t\tWeder ISIN, WKN noch Namen aktiviert für '"+element.getDescription()+"'");
        return false;
    }

    protected boolean matches(ElementDescCorrelation correlation, Map<String, InformationCarrier> carrierMap) {
        // correct in e.g. correctIsin means the mapped value fits
        // like ABCD in database has a mapping to 1234 and the extracted value has 1234
        // this would be a match

        if(compare(carrierMap.getOrDefault("isin", null), correlation.getWsIsin())) return true;
        if(compare(carrierMap.getOrDefault("wkn", null), correlation.getWsWkn())) return true;
        return compare(carrierMap.getOrDefault("name", null), correlation.getWsDescription());
    }

    @Override
    protected void correctCarrierValues(Map<String, InformationCarrier> carrierMap, ElementSelection selection) {
        for(InformationCarrier carrier : carrierMap.values()) {
            carrier.setIsin(selection.getIsin());
        }

    }

    @Override
    protected boolean prepareCarrierAndStatements(Task<Void> task, WebsiteElement websiteElement, Map<String, InformationCarrier> preparedCarrierMap) {

        for (var correlation : websiteElement.getElementIdentCorrelations()) {
            if(task.isCancelled()) return true;

            // create an information carrier with the basic information
            // each correlation is a database table column for which a carrier and a statement is created
            var informationCarrier = prepareCarrier(correlation, null);
            preparedCarrierMap.put(correlation.getDbColName(), informationCarrier);

            // create a sql statement with the basic information
            // row names stay the same
            // by excluding some column names at the statement creation stage the information of these carriers is
            // ignored in the "setStatementExtractedData" function
            // the ignored columns are in general whose that are only used for matching purposes and do not exist inside
            // the table the data is inserted to
            var statement = prepareStatement(connection, informationCarrier);
            if (statement != null && !ignoreIdentCorrelationColumns.contains(correlation.getDbColName())) {
                preparedStatements.put(correlation.getDbColName(), statement);
            }
        }
        return false;
    }

}
