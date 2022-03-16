package de.tud.inf.mmt.wmscrape.gui.tabs.historic.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction.InformationCarrier;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction.TableExtraction;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TableHistoricExtraction extends TableExtraction {
    private String isin;

    public TableHistoricExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier) {
        String dbColName = carrier.getDbColName();

        String sql = "INSERT INTO `"+carrier.getDbTableName()+"` (`"+dbColName+"`, isin, datum) VALUES(?,'"+isin+"',?) ON DUPLICATE KEY UPDATE `" +
                dbColName+"`=VALUES(`"+dbColName+"`);";

        try {
            return connection.prepareStatement(sql);

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
    protected boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations) {
        // check that at least date is set
        for(var corr : correlations) {
            if(corr.getDbColName().equals("datum") && corr.getIdentType() != IdentType.DEAKTIVIERT) return true;
        }

        log("ERR:\t\tDatum nicht aktiviert für '"+element.getDescription()+"'");
        return false;
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
    protected boolean matches(ElementDescCorrelation descCorrelation, Map<String, InformationCarrier> carrierMap) {
        return false;
    }

    @Override
    protected void correctCarrierValues(Map<String, InformationCarrier> carrierMap, ElementSelection selection) {

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
            if(correlation.getDbColName().equals("datum")) continue;
            var statement = prepareStatement(connection, informationCarrier);
            if (statement != null) {
                preparedStatements.put(correlation.getDbColName(), statement);
            }
        }
        return false;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }
}
