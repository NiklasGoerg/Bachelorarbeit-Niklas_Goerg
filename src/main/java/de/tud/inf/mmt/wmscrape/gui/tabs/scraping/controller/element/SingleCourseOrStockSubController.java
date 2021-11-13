package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Lazy
public class SingleCourseOrStockSubController {

    @FXML protected TableView<ElementSelection> selectionTable;
    @FXML protected TableView<ElementIdentCorrelation> columnCorrelationTable;

    @Autowired
    protected ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    protected ScrapingTabManager scrapingTabManager;

    @FXML
    protected void initialize() {
        WebsiteElement websiteElement = scrapingElementsTabController.getSelectedElement();
        scrapingTabManager.initStockSelectionTable(websiteElement, selectionTable, true);
        scrapingTabManager.initCorrelationTable(websiteElement, columnCorrelationTable, MultiplicityType.EINZELWERT);
    }

    public ObservableList<ElementSelection> getSelections() {
        return selectionTable.getItems();
    }

    public ObservableList<ElementIdentCorrelation> getDbCorrelations() {
        return columnCorrelationTable.getItems();
    }
}
