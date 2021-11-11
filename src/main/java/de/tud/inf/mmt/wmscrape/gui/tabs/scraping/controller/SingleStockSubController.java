package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.ElementCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Lazy
public class SingleStockSubController {

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
        scrapingTabManager.initStockCorrelationTable(websiteElement, stockDbColumnTable);
    }

    public ObservableList<ElementSelection> getStockSelections() {
        return stockSelectionTable.getItems();
    }

    public ObservableList<ElementCorrelation> getStockDbCorrelations() {
        return stockDbColumnTable.getItems();
    }
}
