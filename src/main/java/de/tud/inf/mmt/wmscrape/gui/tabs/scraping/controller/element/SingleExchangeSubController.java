package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingElementsTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingCourseOrExchangeManager;
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

import static de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes.identTypeDeactivated;

@Controller
@Lazy
public class SingleExchangeSubController {

    @FXML protected TableView<ElementSelection> exchangeSelectionTable;
    @FXML private ChoiceBox<IdentType> dateIdentTypeChoiceBox;
    @FXML private TextField dateIdentField;
    @FXML private ChoiceBox<IdentType> exchangeIdentTypeChoiceBox;
    @FXML private TextField exchangeIdentField;

    @Autowired
    protected ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    protected ScrapingCourseOrExchangeManager manager;

    protected List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();

    @FXML
    protected void initialize() {
        dateIdentTypeChoiceBox.getItems().addAll(identTypeDeactivated);
        dateIdentTypeChoiceBox.setValue(IdentType.DEAKTIVIERT);
        exchangeIdentTypeChoiceBox.getItems().addAll(identTypeDeactivated);
        exchangeIdentTypeChoiceBox.setValue(IdentType.XPATH);

        WebsiteElement websiteElement = scrapingElementsTabController.getSelectedElement();
        manager.initExchangeSelectionTable(websiteElement, exchangeSelectionTable, true);

        elementIdentCorrelations = manager.initExchangeCorrelations(dateIdentTypeChoiceBox,
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
