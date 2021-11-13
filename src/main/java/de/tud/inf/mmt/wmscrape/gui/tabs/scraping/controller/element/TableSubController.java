package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeSimple;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTableManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Lazy
public class TableSubController extends SingleCourseOrStockSubController{

    @FXML private TableView<ElementDescCorrelation> elementDescCorrelationTableView;
    @FXML private ChoiceBox<IdentTypeSimple> tableIdentChoiceBox;
    @FXML private TextField tableIdentField;

    private WebsiteElement websiteElement;

    @Autowired
    private ScrapingTableManager scrapingTableManager;

    @FXML
    @Override
    protected void initialize() {

        websiteElement = scrapingElementsTabController.getSelectedElement();

        if(websiteElement.getContentType() != ContentType.WECHSELKURS) {
            scrapingTableManager.initStockSelectionTable(websiteElement, selectionTable, false);
            scrapingTableManager.initCourseOrStockDescriptionTable(websiteElement, elementDescCorrelationTableView);
        } else {
            scrapingTableManager.initExchangeSelectionTable(websiteElement, selectionTable, false);
            selectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            scrapingTableManager.initExchangeDescriptionTable(websiteElement,elementDescCorrelationTableView);
            elementDescCorrelationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        scrapingTableManager.initCorrelationTable(websiteElement, columnCorrelationTable, MultiplicityType.TABELLE);


        tableIdentChoiceBox.getItems().addAll(IdentTypeSimple.values());
        tableIdentChoiceBox.setValue(websiteElement.getTableIdenType());
        tableIdentChoiceBox.getSelectionModel().selectedItemProperty().addListener((o,ov,nv) -> websiteElement.setTableIdenType(nv));
        tableIdentField.setText(websiteElement.getTableIdent());
        tableIdentField.textProperty().addListener((o,ov,nv) -> websiteElement.setTableIdent(nv));
    }

    public ObservableList<ElementDescCorrelation> getElementDescCorrelations() {
        return elementDescCorrelationTableView.getItems();
    }
}
