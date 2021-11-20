package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;

public abstract class SingleExtraction extends GeneralExtraction implements Extraction {

    protected SingleExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    public void extract(WebsiteElement element) {
        InformationCarrier carrier;
        preparedStatements = new HashMap<>();
        PreparedStatement statement;
        String data;

        // it's a list but due to ui restraints containing only one selection
        for (var selection : element.getElementSelections()) {
            if(!selection.isSelected()) continue;

            for (var ident : element.getElementIdentCorrelations()) {
                if(ident.getIdentType() == IdentType.DEAKTIVIERT) continue;

                carrier = prepareCarrier(ident, selection);

                data = scraper.findText(carrier.getIdentType(), carrier.getIdentifier(), carrier.getDbColName());

                if(data.equals("")) {
                    log("FEHLER: Keine Daten enthalten in "+ident);
                }

                data = processData(carrier, data);

                if(isValid(data, ident.getColumnDatatype())) {
                    statement = prepareStatement(connection, carrier);
                    if (statement != null) {
                        preparedStatements.put(carrier.getDbColName(), statement);
                        fillStatement(1, statement, data, ident.getColumnDatatype());
                    }
                }
            }
            break;
        }
        storeInDb();
    }
}
