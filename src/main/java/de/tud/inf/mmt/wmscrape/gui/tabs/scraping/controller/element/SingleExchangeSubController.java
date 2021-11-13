package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@Lazy
public class SingleExchangeSubController {

    @FXML protected TableView<ElementSelection> exchangeSelectionTable;
    @FXML private ChoiceBox<IdentTypeDeactivated> dateIdentTypeChoiceBox;
    @FXML private TextField dateIdentField;
    @FXML private ChoiceBox<IdentTypeDeactivated> exchangeIdentTypeChoiceBox;
    @FXML private TextField exchangeIdentField;

    @Autowired
    protected ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    protected ScrapingTabManager scrapingTabManager;

    protected List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();

    @FXML
    protected void initialize() {
        dateIdentTypeChoiceBox.getItems().addAll(IdentTypeDeactivated.values());
        dateIdentTypeChoiceBox.setValue(IdentTypeDeactivated.DEAKTIVIERT);
        exchangeIdentTypeChoiceBox.getItems().addAll(IdentTypeDeactivated.values());
        exchangeIdentTypeChoiceBox.setValue(IdentTypeDeactivated.XPATH);

        WebsiteElement websiteElement = scrapingElementsTabController.getSelectedElement();
        scrapingTabManager.initExchangeSelectionTable(websiteElement, exchangeSelectionTable);

        elementIdentCorrelations = scrapingTabManager.initExchangeCorrelations(dateIdentTypeChoiceBox,
                dateIdentField, exchangeIdentTypeChoiceBox,
                exchangeIdentField, websiteElement);

    }

    public ObservableList<ElementSelection> getExchangeSelections() {
        return exchangeSelectionTable.getItems();
    }

    public List<ElementIdentCorrelation> getElementCorrelations() {
        return elementIdentCorrelations;
    }


}
