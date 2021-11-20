package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SingleCourseOrStockExtraction extends SingleExtraction {

    public SingleCourseOrStockExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    @Override
    protected InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection) {
        carrier.setDbColName(correlation.getDbColName());
        carrier.setDbTableName(correlation.getDbTableName());
        carrier.setIsin(selection.getIsin());
        return carrier;
    }


    @Override
    protected PreparedStatement prepareStatement(Connection connection, InformationCarrier correlation) {
        String dbColName = correlation.getDbColName();

        String sql = "INSERT INTO "+correlation.getDbTableName()+" ("+dbColName+", isin, datum) VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName+"=VALUES("+dbColName+");";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(2, correlation.getIsin());
            statement.setDate(3, correlation.getDate());
            return statement;

        } catch (SQLException e) {
            handleSqlException(correlation, e);
        }
        return null;
    }



}