package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.scraping;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public abstract class SingleElementExtraction extends GeneralExtraction implements Scraping {

    private String tableName;

    protected SingleElementExtraction(Connection connection, SimpleStringProperty logText, String tableName) {
        super(connection, logText);
        this.tableName = tableName;
    }

    public void extract(WebsiteElement element) {
        PreparedCorrelation preparedCorrelation;
        preparedStatements = new HashMap<>();
        PreparedStatement statement;
        String dbColName;

        // it's a list but due to ui restraints containing only one selection
        for (var selection : element.getElementSelections()) {
            for(var ident : element.getElementIdentCorrelations()) {

                preparedCorrelation = prepareCorrelation(ident, selection);

                String data = findData(preparedCorrelation);
                data = sanitize(data, preparedCorrelation.getDatatype());

                if(isValid(data, ident.getColumnDatatype())) {
                    dbColName = preparedCorrelation.getDbColName();

                    if(preparedStatements.containsKey(dbColName)) {
                        statement = preparedStatements.get(dbColName);
                    } else {
                        statement = prepareStatement(connection, preparedCorrelation);
                        if(statement != null) {
                            preparedStatements.put(dbColName, statement);
                        }
                    }

                    fillStatement(statement, data, ident.getColumnDatatype());
                }
            }
            break;
        }
        storeInDb();

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected PreparedStatement prepareStatement(Connection connection, PreparedCorrelation correlation) {
        String dbColName = correlation.getDbColName();

        String sql = "INSERT INTO "+tableName+" (isin, datum, " + dbColName + ") VALUES(?,?,?) ON DUPLICATE KEY UPDATE " +
                dbColName + "=VALUES(" + dbColName + ");";
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            log("FEHLER: SQL-Statement Erstellung. Spalte '"+dbColName+"' der Tabelle "+tableName
                    +". "+e.getMessage()+" <-> "+e.getCause());
            e.printStackTrace();
        }
        return null;
    }

}
