package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeSimple;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
@Lazy
public class TableSubController extends SingleCourseOrStockSubController{

    @FXML private TableView<ElementDescCorrelation> elementDescCorrelationTableView;
    @FXML private ChoiceBox<IdentTypeSimple> tableIdentChoiceBox;
    @FXML private TextField tableIdentField;

    private WebsiteElement websiteElement;

    @FXML
    @Override
    protected void initialize() {

        websiteElement = scrapingElementsTabController.getSelectedElement();

        if(websiteElement.getContentType() != ContentType.WECHSELKURS) {
            scrapingTabManager.initStockSelectionTable(websiteElement, selectionTable, false);
        } else {
            scrapingTabManager.initExchangeSelectionTable(websiteElement, selectionTable, false);
            selectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        scrapingTabManager.initCorrelationTable(websiteElement, columnCorrelationTable, MultiplicityType.TABELLE);

        if(websiteElement.getContentType() != ContentType.WECHSELKURS) {
            scrapingTabManager.initCourseOrStockDescriptionTable(websiteElement, elementDescCorrelationTableView);
        } else {
            scrapingTabManager.initExchangeDescriptionTable(websiteElement,elementDescCorrelationTableView);
            elementDescCorrelationTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        tableIdentChoiceBox.getItems().addAll(IdentTypeSimple.values());
        tableIdentChoiceBox.setValue(websiteElement.getTableIdenType());
        tableIdentChoiceBox.getSelectionModel().selectedItemProperty().addListener((o,ov,nv) -> websiteElement.setTableIdenType(nv));
        tableIdentField.setText(websiteElement.getTableIdent());
        tableIdentField.textProperty().addListener((o,ov,nv) -> websiteElement.setTableIdent(nv));
    }

    public ObservableList<ElementDescCorrelation> getElementDescCorrelations() {
        return elementDescCorrelationTableView.getItems();
    }

    public void setOriginalElementValues(String url, IdentTypeSimple type) {
        websiteElement.setTableIdent(type.name());
        websiteElement.setInformationUrl(url);
    }
}
