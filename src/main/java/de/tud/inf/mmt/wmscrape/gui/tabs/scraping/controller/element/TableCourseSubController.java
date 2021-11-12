package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Lazy
public class TableCourseSubController extends SingleCourseOrStockSubController{

    @FXML
    private TableView<ElementDescCorrelation> elementDescCorrelationTableView;

    @FXML
    @Override
    protected void initialize() {
        WebsiteElement websiteElement = scrapingElementsTabController.getSelectedElement();
        scrapingTabManager.initStockSelectionTable(websiteElement, stockSelectionTable, false);
        scrapingTabManager.initCourseOrStockCorrelationTable(websiteElement, stockDbColumnTable, MultiplicityType.TABELLE);
        scrapingTabManager.initCourseDescriptionTable(websiteElement, elementDescCorrelationTableView);

    }

    public TableView<ElementDescCorrelation> getElementDescCorrelationTableView() {
        return elementDescCorrelationTableView;
    }

    public ObservableList<ElementDescCorrelation> getElementDescCorrelations() {
        return elementDescCorrelationTableView.getItems();
    }
}
