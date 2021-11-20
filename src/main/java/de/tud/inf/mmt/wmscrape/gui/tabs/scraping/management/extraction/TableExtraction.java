package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

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

    protected abstract boolean validIdentCorrelations(List<ElementIdentCorrelation> elementIdentCorrelations);

    protected abstract void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection);

    public void extract(WebsiteElement element) {
        Map<String, InformationCarrier> preparedCarrierMap = new HashMap<>();
        InformationCarrier informationCarrier;
        preparedStatements = new HashMap<>();
        PreparedStatement statement;
        List<WebElement> rows;


        // e.g. stock/course needs isin, exchange the name
        if(!validIdentCorrelations(element.getElementIdentCorrelations())) return;


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
        WebElement webElement = getTable(element);
        if(webElement == null) {
            log("FEHLER: Tabelle für "+element.getInformationUrl()+" nicht gefunden.");
            return;
        }

        for (var selection : element.getElementSelections()) {
            if(selection == null || !selection.isSelected()) continue;

            // get rows
            rows = getRows(webElement);

            if(rows == null || rows.size() == 0) {
                log("FEHLER: Tabelle für "+element.getInformationUrl()+" enhält keine Zeilen (<tr>)");
                return;
            }

            // add/update the sql statement information
            // e.g. setting the isin or exchange name
            // reset data to null
            updateStatements(preparedStatements, selection);

            // update the carrier information
            // also isin/currency
            for(InformationCarrier correlation : preparedCarrierMap.values()) {
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

    protected WebElement getTable(WebsiteElement websiteElement) {
        WebElement element = scraper.extractElementFromRoot(websiteElement.getTableIdenType(), websiteElement.getTableIdent());
        scraper.highlightElement(element, "Tabelle");
        return element;
    }

    protected List<WebElement> getRows(WebElement from) {
        List<WebElement> elements = scraper.extractElementsFromContext(from, IdentType.TAG, "tr", false);

        if(scraper.isHeadless()) return elements;

        int i=1;
        for(WebElement element : elements) {
            scraper.highlightElement(element, "Zeile "+i);
            i++;
        }
        return elements;
    }

    protected String getTextData(SearchContext context, InformationCarrier carrier) {
        return scraper.findTextInContext(context, carrier.getIdentType(), carrier.getIdentifier(),
                carrier.getDbColName(), false);
    }

    private void searchInsideRow(Map<String, InformationCarrier> carrierMap, WebElement row) {
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

    private boolean matches(List<ElementDescCorrelation> descCorrelations, Map<String, InformationCarrier> correlationDbColMap) {

        String extractedIsin = correlationDbColMap.getOrDefault("isin", null).getExtractedData();
        String extractedWkn = correlationDbColMap.getOrDefault("wkw", null).getExtractedData();
        String extractedDesc = correlationDbColMap.getOrDefault("name", null).getExtractedData();

        // check matching isin
        if(extractedIsin != null && extractedIsin.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctIsin = descCorrelation.getWsIsin();
                if(compare(extractedIsin, correctIsin)) {
                    return true;
                }
            }
        }

        // check matching wkn
        if(extractedWkn != null && extractedWkn.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctWkn = descCorrelation.getWsWkn();
                if(compare(extractedWkn, correctWkn)) {
                    return true;
                }
            }
        }

        // check matching description
        if(extractedDesc != null && extractedDesc.length() > 0) {
            for(var descCorrelation : descCorrelations) {
                var correctDescription = descCorrelation.getWsDescription();
                if(compare(extractedDesc, correctDescription)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean compare(String websiteData, String dbData) {
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

    private void partialMatchLog(String extracted, String field) {
        log("FEHLER: " + field + " stimmt nicht direkt mit " + extracted + " überein. Die Auswahl-Regex sollte angepasst werden");
    }

}
