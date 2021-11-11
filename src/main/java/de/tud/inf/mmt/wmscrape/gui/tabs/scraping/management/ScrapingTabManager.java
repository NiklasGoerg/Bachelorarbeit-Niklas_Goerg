package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.SingleStockSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockDataTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockRepository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingTabManager {

    @Autowired
    private WebsiteRepository websiteRepository;
    @Autowired
    private WebsiteElementRepository websiteElementRepository;
    @Autowired
    private ElementSelectionRepository elementSelectionRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    private SingleStockSubController singleStockSubController;
    @Autowired
    private ElementCorrelationRepository elementCorrelationRepository;

    public Website createNewWebsite(String description) {
        Website website = new Website(description);
        websiteRepository.save(website);
        return website;
    }

    public WebsiteElement createNewElement(String description, ContentType contentType, MultiplicityType multiplicityType) {
        WebsiteElement element = new WebsiteElement(description, contentType, multiplicityType);
        websiteElementRepository.save(element);
        return element;
    }

    public void deleteSpecificWebsite(Website website) {
        // fix for not working orphan removal
        website.setWebsiteElements(new ArrayList<>());
        websiteRepository.delete(website);
    }

    public void deleteSpecificElement(WebsiteElement element) {
        element.setElementSelections(new ArrayList<>());
        element.setElementCorrelations(new ArrayList<>());
        websiteElementRepository.delete(element);
    }

    public ObservableList<Website> initWebsiteList(ListView<Website> websiteListView) {
        ObservableList<Website> websiteObservableList = FXCollections.observableList(websiteRepository.findAll());
        websiteListView.setItems(websiteObservableList);
        return websiteObservableList;
    }

    public ObservableList<WebsiteElement> initWebsiteElementList(ListView<WebsiteElement> elementListView) {
        ObservableList<WebsiteElement> elementObservableList = FXCollections.observableList(websiteElementRepository.findAll());
        elementListView.setItems(elementObservableList);
        return elementObservableList;
    }

    public List<Website> getWebsites() {
        return websiteRepository.findAll();
    }

    public List<WebsiteElement> getElements() {
        return websiteElementRepository.findAll();
    }

    public Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle(".bad-input");
        return tooltip;
    }

    public void saveWebsite(Website website) {
        websiteRepository.save(website);
    }

    public static void loadSubMenu(Object controllerClass, String resource, BorderPane control) {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(resource));
        fxmlLoader.setControllerFactory(param -> controllerClass);
        Parent scene;

        try {
            scene = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        control.centerProperty().setValue(scene);
    }

//    #########################
//    Single Stock section
//    #########################

    public WebsiteElement getFreshWebsiteElement(int id) {
        return websiteElementRepository.getById(id);
    }

    @Transactional
    public void choiceBoxSetWebsiteElement(ChoiceBox<Website> choiceBox, int id) {
        // needs a fresh load in one transaction
        // websitEelement from table hast not a loaded website (lazy load) and an ended session
        choiceBox.setValue(getFreshWebsiteElement(id).getWebsite());
    }

    @Transactional
    public ObservableList<ElementSelection> initStockSelectionTable(WebsiteElement staleElement,TableView<ElementSelection> table ) {

        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement.getId());
        prepareStockSelectionTable(table);
        return fillStockSelectionTable(websiteElement, table);
    }

    private void prepareStockSelectionTable(TableView<ElementSelection> table) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Bezeichnung");
        TableColumn<ElementSelection, String> stockIsinColumn = new TableColumn<>("Isin");

        selectedColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectedColumn));
        selectedColumn.setCellValueFactory(row -> {
            SimpleBooleanProperty sbp = row.getValue().selectedProperty();
            sbp.addListener( (o, ov, nv) -> {
                sbp.set(nv);
                if(nv) deselectOther(row);
            });
            return sbp;
        });

        stockNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        stockIsinColumn.setCellValueFactory(new PropertyValueFactory<>("isin"));

        table.getColumns().add(selectedColumn);
        table.getColumns().add(stockNameColumn);
        table.getColumns().add(stockIsinColumn);
    }

    private ObservableList<ElementSelection> fillStockSelectionTable(WebsiteElement websiteElement, TableView<ElementSelection> table) {
        ObservableList<ElementSelection> stockSelections = FXCollections.observableArrayList();
        ArrayList<Stock> addedStockSelection = new ArrayList<>();

        for(ElementSelection elementSelection : websiteElement.getElementSelections()) {
            stockSelections.add(elementSelection);
            addedStockSelection.add(elementSelection.getStock());
        }

        for(Stock stock : stockRepository.findAll()) {
            if(!addedStockSelection.contains(stock)) {
                ElementSelection elementSelection = new ElementSelection(
                        stock.getName(),
                        websiteElement,
                        stock);

                addedStockSelection.add(stock);
                stockSelections.add(elementSelection);
            }
        }

        table.getItems().addAll(stockSelections);
        return stockSelections;
    }

    private void deselectOther(TableColumn.CellDataFeatures<ElementSelection, Boolean> row) {

        ElementSelection selectedOne = row.getValue();
        for(ElementSelection selection : row.getTableView().getItems()) {
            if(!selectedOne.equals(selection)) {
                selection.setSelected(false);
            }
        }
    }

    @Transactional
    public ObservableList<ElementCorrelation> initStockCorrelationTable(WebsiteElement staleElement,TableView<ElementCorrelation> table ) {
        // load anew because the element from the table has no session attached anymore and therefore can't resolve
        // lazy evaluation
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement.getId());
        prepareStockCorrelationTable(table);
        return fillStockCorrelationTable(websiteElement, table);
    }

    private void prepareStockCorrelationTable(TableView<ElementCorrelation> table) {

        TableColumn<ElementCorrelation, String> dbColNameColumn = new TableColumn<>("Datenbank");
        TableColumn<ElementCorrelation, String> identTypeColumn = new TableColumn<>("Selektionstyp");
        TableColumn<ElementCorrelation, String> representationColumn = new TableColumn<>("Webseitenrepräsentation");
        representationColumn.setMinWidth(210);

        // DbColName
        dbColNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockDataTableColumn().getName()));

        // choiceBox
        identTypeColumn.setCellFactory(col -> {
            TableCell<ElementCorrelation, String> cell = new TableCell<>();
            ChoiceBox<IdentTypeDeactivated> choiceBox = new ChoiceBox<>();
            choiceBox.getItems().addAll(IdentTypeDeactivated.values());

            // update value
            choiceBox.valueProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null) {
                    cell.getTableRow().getItem().setIdentType(nv.name());
                }
            });

            // set initial value
            cell.graphicProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow() != null && cell.getTableRow().getItem() != null && cell.getTableRow().getItem().getIdentType() != null) {
                    choiceBox.setValue(IdentTypeDeactivated.valueOf(cell.getTableRow().getItem().getIdentType()));
                }
            });

            cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(choiceBox));
            return cell;
        });

        // representation
        representationColumn.setCellValueFactory(param -> {
            SimpleStringProperty ssp = param.getValue().representationProperty();
            ssp.addListener((o, ov, nv) -> ssp.set(nv));
            return ssp;
        });
        representationColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        table.getColumns().add(dbColNameColumn);
        table.getColumns().add(identTypeColumn);
        table.getColumns().add(representationColumn);
    }

    private ObservableList<ElementCorrelation> fillStockCorrelationTable(WebsiteElement websiteElement, TableView<ElementCorrelation> table) {
        ObservableList<ElementCorrelation> stockSelections = FXCollections.observableArrayList();
        ArrayList<StockDataTableColumn> addedStockColumns = new ArrayList<>();

        for (ElementCorrelation elementCorrelation : websiteElement.getElementCorrelations()) {
            stockSelections.add(elementCorrelation);
            addedStockColumns.add(elementCorrelation.getStockDataTableColumn());
        }

        for(StockDataTableColumn column : stockDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column)) {
                addedStockColumns.add(column);
                stockSelections.add(new ElementCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(stockSelections);
        return stockSelections;
    }

    public void saveWebsiteElementSettings(WebsiteElement websiteElement) {


        for (ElementSelection selection : singleStockSubController.getStockSelections()) {
            if(selection.isChanged()) {
                elementSelectionRepository.save(selection);
            }
        }

        for (ElementCorrelation correlation : singleStockSubController.getStockDbCorrelations()) {
            if(correlation.isChanged()) {
                elementCorrelationRepository.save(correlation);
            }
        }

        websiteElementRepository.save(websiteElement);
    }
}
