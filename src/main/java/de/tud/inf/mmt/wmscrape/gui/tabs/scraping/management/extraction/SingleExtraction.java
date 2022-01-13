package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;

public abstract class SingleExtraction extends ExtractionGeneral implements Extraction {

    protected SingleExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        super(connection, logText, scraper, date);
    }

    /**
     * starts the extraction process for a single website element configuration
     *
     * @param element the website element configuration to extract data for
     * @param task the task the process is running in
     * @param progress the selection/row progress property bound to the javafx progress bar
     */
    public void extract(WebsiteElement element, Task<Void> task, SimpleDoubleProperty progress) {
        var identCorrelations = element.getElementIdentCorrelations();
        var elementSelections = element.getElementSelections();
        preparedStatements.clear();
        InformationCarrier carrier;


        double currentProgress = -1;
        double maxProgress = elementSelections.size();

        scraper.resetIdentDataBuffer();
        logStart(element.getDescription());

        // it's a list but due to ui restraints containing only one selection
        for (var selection : elementSelections) {
            if(task.isCancelled()) return;
            if(!selection.isSelected()) continue;

            for (var ident : identCorrelations) {
                if(task.isCancelled()) return;

                currentProgress++;
                progress.set(currentProgress / maxProgress);
                carrier = prepareCarrier(ident, selection);
                String data = null;

                if(ident.getIdentType() != IdentType.DEAKTIVIERT) {

                    data = getTextData(null, carrier);

                    if (data.isBlank()) {
                        log("ERR:\t\tKeine Daten enthalten für " + carrier.getDbColName() + " unter '" + ident.getIdentification() + "'");
                    }

                    data = processData(carrier, data);

                    // override invalid / blank data
                    if(!isValid(data, ident.getColumnDatatype(), ident.getDbColName())) {
                        data = null;
                    }
                }

                PreparedStatement statement = prepareStatement(connection, carrier);
                if (statement != null) {
                    preparedStatements.put(carrier.getDbColName(), statement);
                    fillStatement(1, statement, data, ident.getColumnDatatype());
                }
            }
            break;
        }
        progress.set(1);

        logSuccess(element.getDescription());

        storeInDb();
    }

    private void logSuccess(String description) {
        log("\nINFO:\tExtraktion abgeschlossen für: "+description+
                "\n\n----------------------------------------------------------------------------\n");
    }

}
