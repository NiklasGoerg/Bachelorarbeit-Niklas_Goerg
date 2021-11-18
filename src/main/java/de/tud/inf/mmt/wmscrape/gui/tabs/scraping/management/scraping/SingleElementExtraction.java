package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.scraping;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;

public abstract class SingleElementExtraction extends GeneralExtraction implements Scraping {

    protected SingleElementExtraction(Connection connection, SimpleStringProperty logText, String tableName) {
        super(connection, logText);
    }

    public void extract(WebsiteElement element) {
        PreparedCorrelation preparedCorrelation;
        preparedStatements = new HashMap<>();
        PreparedStatement statement;
        String dbColName;


        // it's a list but due to ui restraints containing only one selection
        for (var selection : element.getElementSelections()) {
            if(!selection.isSelected()) continue;

            for (var ident : element.getElementIdentCorrelations()) {
                if(ident.getIdentType() == IdentType.DEAKTIVIERT) continue;

                preparedCorrelation = prepareCorrelation(ident, selection);
                String data = findData(preparedCorrelation);

                if (ident.getRegex() != null && !ident.getRegex().trim().equals("")) {
                    var tmp =  findFirst(ident.getRegex(), data);
                    log("INFO: Regex angewandt. '"+tmp+"' aus '"+data+"' extrahiert.");
                }

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

                    if (statement != null) {
                        fillStatement(statement, data, ident.getColumnDatatype());
                    }
                }
            }
            break;
        }
        storeInDb();
    }

}
