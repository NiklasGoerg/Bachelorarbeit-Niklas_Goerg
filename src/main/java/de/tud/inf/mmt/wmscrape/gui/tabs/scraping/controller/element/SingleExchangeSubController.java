package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.ScrapingTabManager;
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
public class SingleExchangeSubController {

    @FXML private ChoiceBox<IdentTypeDeactivated> dateIdentTypeChoiceBox;
    @FXML private TextField dateIdentField;
    @FXML private ChoiceBox<IdentType> exchangeIdentTypeChoiceBox;
    @FXML private TextField exchangeIdentField;
    @FXML private TableView<ElementSelection> exchangeSelectionTable;

    @Autowired
    private ScrapingElementsTabController scrapingElementsTabController;
    @Autowired
    private ScrapingTabManager scrapingTabManager;

    @FXML
    private void initialize() {
        dateIdentTypeChoiceBox.getItems().addAll(IdentTypeDeactivated.values());
        dateIdentTypeChoiceBox.setValue(IdentTypeDeactivated.DEAKTIVIERT);
        exchangeIdentTypeChoiceBox.getItems().addAll(IdentType.values());
        exchangeIdentTypeChoiceBox.setValue(IdentType.XPATH);

        WebsiteElement websiteElement = scrapingElementsTabController.getSelectedElement();
        //scrapingTabManager.initExchangeSelectionTable(websiteElement, exchangeSelectionTable);
    }

    public ObservableList<ElementSelection> getExchangeSelectionTable() {
        return exchangeSelectionTable.getItems();
    }
}
