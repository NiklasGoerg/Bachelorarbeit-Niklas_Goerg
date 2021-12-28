package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebElementInContext;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

public abstract class TableExtraction extends ExtractionGeneral implements Extraction {

    private final static List<String> doNotSaveColumns = List.of("isin", "wkn", "name", "typ");

    protected TableExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    protected abstract boolean validIdentCorrelations(WebsiteElement element, List<ElementIdentCorrelation> correlations);

    protected abstract void updateStatements(Map<String, PreparedStatement> statements, ElementSelection selection);

    protected abstract boolean matches(ElementDescCorrelation descCorrelation, Map<String, InformationCarrier> carrierMap);

    protected abstract void correctCarrierValues(Map<String, InformationCarrier> carrierMap, ElementSelection selection);

    public void extract(WebsiteElement element, Task<Void> task, SimpleDoubleProperty progress) {
        var identCorrelations = element.getElementIdentCorrelations();
        var elementSelections = element.getElementSelections();
        Map<String, InformationCarrier> preparedCarrierMap = new HashMap<>();
        InformationCarrier informationCarrier;
        preparedStatements = new HashMap<>();
        List<WebElementInContext> rows;
        PreparedStatement statement;
        double currentProgress;
        double maxProgress;

        logStart(element.getDescription());

        // e.g. stock/course needs isin or wkn or the name
        if(!validIdentCorrelations(element, identCorrelations)) return;

        for (var correlation : identCorrelations) {
            if(task.isCancelled()) return;

            // create an information carrier with the basic information
            informationCarrier = prepareCarrier(correlation, null);
            preparedCarrierMap.put(correlation.getDbColName(), informationCarrier);

            // create a sql statement with the basic information
            // row names stay the same
            statement = prepareStatement(connection, informationCarrier);
            if (statement != null && !doNotSaveColumns.contains(correlation.getDbColName())) {
                preparedStatements.put(correlation.getDbColName(), statement);
            }
        }

        // get the table
        WebElementInContext table = getTable(element);
        if(table == null) {
            log("ERR:\t\tTabelle für "+element.getInformationUrl()+" nicht gefunden.");
            return;
        }

        // get rows
        rows = getRows(table);

        if(rows == null || rows.isEmpty()) {
            log("ERR:\t\tTabelle für "+element.getInformationUrl()+" enhält keine Zeilen (<tr>)");
            return;
        }

        currentProgress = 0;
        maxProgress = rows.size();

        // don't wait for elements inside the table
        scraper.waitForWsElements(false);

        // search each row for a matching stock/exchange
        for(var row : rows) {
            // looks for the information inside one row
            // adds it to the corresponding carriers
            if(task.isCancelled()) return;
            searchInsideRow(preparedCarrierMap, row);
            processSelectionsForRow(elementSelections, preparedCarrierMap);
            resetCarriers(preparedCarrierMap);
            currentProgress++;
            progress.set(currentProgress/maxProgress);
            scraper.resetIdentBuffer();
        }
        scraper.waitForWsElements(true);

        storeInDb();

        logMatches(elementSelections, element.getDescription());
    }

    private void logMatches(List<ElementSelection> selections, String description) {

        StringBuilder success = new StringBuilder("\n");
        StringBuilder fail = new StringBuilder("\n");

        for (var s : selections) {
            if(s.isSelected()) {
                if (!s.wasExtracted()){
                    fail.append("\t\t- ").append(s.getDescription()).append("\n");
                } else {
                    success.append("\t\t- ").append(s.getDescription()).append("\n");
                }
            }
        }

        log("----------------------------------------------------------------------------\n\n" +
                "INFO:\tExtraktion abgeschlossen für: "+description+" \n\n" +
                "INFO:\tErfolgreich extrahiert:\n" +
                success +
                "\nWARN:\tKeine Treffer für:\n" +
                fail+
                "\n----------------------------------------------------------------------------\n");
    }

    private void processSelectionsForRow(List<ElementSelection> selections,
                                         Map<String, InformationCarrier> carrierMap) {
        
        for (var selection : selections) {
            if(selection == null || !selection.isSelected()) continue;

            // checks if selection with its description correlation exists
            // that fits the extracted data
            if(matches(selection.getElementDescCorrelation(), carrierMap)) {

                // another row matches a selection
                if(selection.wasExtracted()) {
                    log("ERR:\t\tFür '"+selection.getDescription()+"' wurden bereits Daten aus der Tabelle " +
                            "importiert. Element wird Ignoriert.");
                }

                // add/update the sql statement information
                // e.g. setting the isin or exchange name
                updateStatements(preparedStatements, selection);

                // set the correct values from the db like isin/wkn/description
                // e.g. a matching wkn has a false isin
                // the isin hast to be corrected in the carrier
                correctCarrierValues(carrierMap, selection);

                // sets the actual data to the prepared statements
                // adds them to the statement batch
                setStatementExtractedData(carrierMap);
                
                selection.isExtracted();
                log("\nINFO:\tTreffer für "+selection.getDescription()+"\n");
                return;
            }
        }

        log("\nINFO:\tKein Treffer in der Zeile.\n");
    }

    private void resetCarriers(Map<String, InformationCarrier> carriers) {
        carriers.values().forEach(c -> c.setExtractedData(null));
    }

    private void setStatementExtractedData(Map<String, InformationCarrier> carrierMap) {
        for(var carrier : carrierMap.values()) {
            var statement = preparedStatements.getOrDefault(carrier.getDbColName(), null);
            if(statement != null) {
                fillStatement(1, statement, carrier.getExtractedData(), carrier.getDatatype());
            }
        }
    }

    private WebElementInContext getTable(WebsiteElement websiteElement) {
        WebElementInContext element = scraper.extractFrameElementFromContext(websiteElement.getTableIdenType(), websiteElement.getTableIdent(), null);

        if(element == null) return null;

        scraper.highlightElement(element.get(), "Tabelle");
        return element;
    }

    private List<WebElementInContext> getRows(WebElementInContext table) {
        List<WebElementInContext> elements;

        elements = scraper.extractAllFramesFromContext(IdentType.XPATH, "//tr[not(th)][not(ancestor::thead)]", table);

        if(scraper.isHeadless() || elements == null || elements.isEmpty()) return elements;

        int i=1;
        for(WebElementInContext element : elements) {
            scraper.highlightElement(element.get(), "Zeile "+i);
            i++;
        }
        return elements;
    }

    private void searchInsideRow(Map<String, InformationCarrier> carrierMap, WebElementInContext row) {
        String data;

        for(InformationCarrier carrier : carrierMap.values()) {
            if(carrier.getIdentType() == IdentType.DEAKTIVIERT) continue;

            data = getTextData(row, carrier);

            if (data.isBlank()) {
                log("ERR:\t\tKeine Daten enthalten für "+carrier.getDbColName()+" unter '"+carrier.getIdentifier()+"'");
            }

            data = processData(carrier, data);

            if (isValid(data, carrier.getDatatype(), carrier.getDbColName())) {
                carrier.setExtractedData(data);
            }
        }
    }

    protected boolean compare(InformationCarrier carrier, String dbData) {
        if (carrier != null ) {
            var websiteData = carrier.getExtractedData();
            if (websiteData != null && !websiteData.isBlank() && !dbData.isBlank()) {
                if (dbData.equals(websiteData)) {
                    // matched
                    return true;
                } else if (dbData.contains(websiteData) || websiteData.contains(dbData) ) {
                    // found inside but no direct match
                    partialMatchLog(dbData, websiteData);
                }
            }
        }
        return false;
    }

    protected void partialMatchLog(String extracted, String field) {
        log("ERR:\t\t"+field+" stimmt nicht direkt mit '"+extracted+"' überein. Die Auswahl-Regex oder Bezeichnung sollte angepasst werden");
    }
}
