package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SingleExchangeExtraction extends SingleExtraction{

    public SingleExchangeExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier correlation) {
        String dbColName = correlation.getDbColName();

        String sql = "INSERT INTO "+correlation.getDbTableName()+" (" + dbColName + ", datum) VALUES(?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDate(2, correlation.getDate());
            return statement;

        } catch (SQLException e) {
            handleSqlException(correlation, e);
        }
        return null;
    }

    @Override
    protected InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection) {
        carrier.setDbColName(correlation.getDbColName());
        carrier.setDbTableName(correlation.getDbTableName());
        return carrier;
    }

}
