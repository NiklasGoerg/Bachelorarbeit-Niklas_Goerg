package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebElementInContext;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TableExtraction extends GeneralExtraction implements Extraction {

    protected TableExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    protected abstract boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations);

    protected abstract void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection);

    protected abstract boolean matches(List<ElementDescCorrelation> descCorrelations, Map<String, InformationCarrier> carrierMap);

    protected abstract void setCorrectValuesFromSelection(Map<String, InformationCarrier> carrierMap, ElementSelection selection);

    public void extract(WebsiteElement element) {
        Map<String, InformationCarrier> preparedCarrierMap = new HashMap<>();
        InformationCarrier informationCarrier;
        preparedStatements = new HashMap<>();
        PreparedStatement statement;
        List<WebElementInContext> rows;


        // e.g. stock/course needs isin or wkn or the name
        if(!validIdentCorrelations(element, element.getElementIdentCorrelations())) return;


        for (var correlation : element.getElementIdentCorrelations()) {
            // create an information carrier with the basic information
            informationCarrier = prepareCarrier(correlation, null);
            preparedCarrierMap.put(correlation.getDbColName(), informationCarrier);

            // create a sql statement with the basic information
            // row names stay the same
            statement = prepareStatement(connection, informationCarrier);
            if (statement != null) {
                preparedStatements.put(correlation.getDbTableName(), statement);
            }
        }

        // get the table
        WebElementInContext table = getTable(element);
        if(table == null) {
            log("FEHLER: Tabelle für "+element.getInformationUrl()+" nicht gefunden.");
            return;
        }

        for (var selection : element.getElementSelections()) {
            if(selection == null || !selection.isSelected()) continue;

            // get rows
            rows = getRows(table);

            if(rows == null || rows.size() == 0) {
                log("FEHLER: Tabelle für "+element.getInformationUrl()+" enhält keine Zeilen (<tr>)");
                return;
            }

            // add/update the sql statement information
            // e.g. setting the isin or exchange name
            updateStatements(preparedStatements, selection);

            // update the carrier information
            // also isin/currency
            for(InformationCarrier correlation : preparedCarrierMap.values()) {
                // TODO necessary?
                correlation.setExtractedData(null);
                correlation.setIsin(selection.getIsin());
            }

            // search each row for a matching stock/exchange
            for(var row : rows) {

                // looks for the information inside one row
                // adds it to the corresponding carriers
                searchInsideRow(preparedCarrierMap, row);

                // checks if selection with its description correlation exists
                // that fits the extracted data
                if(matches(element.getElementDescCorrelations(), preparedCarrierMap)) {

                    // set the correct values from the db like isin/wkn/description
                    // e.g. a matching wkn has a false isin
                    // the isin hast to be corrected in the carrier
                    setCorrectValuesFromSelection(preparedCarrierMap, selection);

                    // sets the actual data to the prepared statements
                    // adds them to the statement batch
                    for(var carrier : preparedCarrierMap.values()) {
                        statement = preparedStatements.get(carrier.getDbColName());
                        if(statement != null) {
                            fillStatement(1, statement, carrier.getExtractedData(), carrier.getDatatype());
                        }
                    }
                }
            }
        }
        storeInDb();
    }


    private WebElementInContext getTable(WebsiteElement websiteElement) {
        WebElementInContext element = scraper.extractFrameElementFromRoot(websiteElement.getTableIdenType(), websiteElement.getTableIdent());

        if(element == null) return null;

        scraper.highlightElement(element.get(), "Tabelle");
        return element;
    }

    private List<WebElementInContext> getRows(WebElementInContext table) {
        List<WebElementInContext> elements = scraper.extractAllFramesFromContext(IdentType.TAG, "tr", table);

        if(scraper.isHeadless()) return elements;

        int i=1;
        for(WebElementInContext element : elements) {
            scraper.highlightElement(element.get(), "Zeile "+i);
            i++;
        }
        return elements;
    }

    private String getTextData(WebElementInContext element, InformationCarrier carrier) {
        return scraper.findTextInContext(carrier.getIdentType(), carrier.getIdentifier(),
                carrier.getDbColName(), element);
    }

    private void searchInsideRow(Map<String, InformationCarrier> carrierMap, WebElementInContext row) {
        String data;

        for(InformationCarrier carrier : carrierMap.values()) {
            if(carrier.getIdentType() == IdentType.DEAKTIVIERT) continue;

            data = getTextData(row, carrier);

            if (data.equals("")) {
                log("FEHLER: Keine Daten enthalten für "+carrier.getDbColName()+" unter "+carrier.getIdentifier());
            }

            data = processData(carrier, data);

            if (isValid(data, carrier.getDatatype())) {
                carrier.setExtractedData(data);
            }
        }
    }

    protected boolean compare(String websiteData, String dbData) {
        if (websiteData != null && websiteData.length() > 0) {
            if (dbData.equals(websiteData)) {
                // matched
                return true;
            } else if (dbData.contains(websiteData)) {
                // found inside but no direct match
                partialMatchLog(dbData, websiteData);
                return false;
            }
        }
        return false;
    }

    protected void partialMatchLog(String extracted, String field) {
        log("FEHLER: " + field + " stimmt nicht direkt mit " + extracted + " überein. Die Auswahl-Regex sollte angepasst werden");
    }

}
