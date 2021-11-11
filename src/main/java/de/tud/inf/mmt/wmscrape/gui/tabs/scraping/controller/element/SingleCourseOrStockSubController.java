package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementCorrelation;
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

    @FXML private TableView<ElementSelection> stockSelectionTable;
    @FXML private TableView<ElementCorrelation> stockDbColumnTable;

    @Autowired
    private ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    private ScrapingTabManager scrapingTabManager;

    @FXML
    private void initialize() {
        WebsiteElement websiteElement = scrapingElementsTabController.getSelectedElement();
        scrapingTabManager.initStockSelectionTable(websiteElement, stockSelectionTable);
        scrapingTabManager.initCourseOrStockCorrelationTable(websiteElement, stockDbColumnTable);
    }

    public ObservableList<ElementSelection> getSelections() {
        return stockSelectionTable.getItems();
    }

    public ObservableList<ElementCorrelation> getDbCorrelations() {
        return stockDbColumnTable.getItems();
    }
}
