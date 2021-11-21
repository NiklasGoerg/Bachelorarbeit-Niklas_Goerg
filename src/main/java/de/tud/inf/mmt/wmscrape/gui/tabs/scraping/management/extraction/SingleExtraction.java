package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

public abstract class SingleExtraction extends GeneralExtraction implements Extraction {

    protected SingleExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    public void extract(WebsiteElement element) {
        List<ElementIdentCorrelation> identCorrelations = element.getElementIdentCorrelations();
        InformationCarrier carrier;
        preparedStatements = new HashMap<>();
        PreparedStatement statement;
        String data;

        if(duplicateIdentifiers(identCorrelations)) return;

        // it's a list but due to ui restraints containing only one selection
        for (var selection : element.getElementSelections()) {
            if(!selection.isSelected()) continue;

            for (var ident : identCorrelations) {
                if(ident.getIdentType() == IdentType.DEAKTIVIERT) continue;

                carrier = prepareCarrier(ident, selection);

                data = scraper.findText(carrier.getIdentType(), carrier.getIdentifier(), carrier.getDbColName());

                if(data.equals("")) {
                    log("ERR:\t\tKeine Daten enthalten für "+ident);
                }

                data = processData(carrier, data);

                if(isValid(data, ident.getColumnDatatype(), ident.getDbColName())) {
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
