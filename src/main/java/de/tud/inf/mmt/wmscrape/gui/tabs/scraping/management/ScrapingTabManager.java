package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleCourseOrStockSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleExchangeSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.TableCourseSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElementRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeTable;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelectionRepository;
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
    private SingleCourseOrStockSubController singleCourseOrStockSubController;
    @Autowired
    private ElementIdentCorrelationRepository elementIdentCorrelationRepository;
    @Autowired
    private CourseDataColumnRepository courseDataColumnRepository;
    @Autowired
    private ExchangeDataColumnRepository exchangeDataColumnRepository;
    @Autowired
    private SingleExchangeSubController singleExchangeSubController;
    @Autowired
    private TableCourseSubController tableCourseSubController;
    @Autowired
    private ElementDescCorrelationRepository elementDescCorrelationRepository;

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

    public WebsiteElement getFreshWebsiteElement(WebsiteElement staleElement) {
        return websiteElementRepository.getById(staleElement.getId());
    }

    @Transactional
    public void choiceBoxSetWebsiteElement(ChoiceBox<Website> choiceBox, WebsiteElement staleElement) {
        // needs a fresh load in one transaction
        // websitEelement from table hast not a loaded website (lazy load) and an ended session
        choiceBox.setValue(getFreshWebsiteElement(staleElement).getWebsite());
    }

    private void createCheckBox(TableColumn<ElementSelection, Boolean> column, boolean singleSelection) {
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setCellValueFactory(row -> {
            SimpleBooleanProperty sbp = row.getValue().selectedProperty();
            sbp.addListener( (o, ov, nv) -> {
                if(singleSelection) {
                    if(nv) deselectOther(row);
                } else {
                    if(nv) addNewElementDescCorrelation(row.getValue());
                    else removeElementDescCorrelation(row.getValue());
                }
            });
            return sbp;
        });
    }

    private void deselectOther(TableColumn.CellDataFeatures<ElementSelection, Boolean> row) {

        ElementSelection selectedOne = row.getValue();
        for(ElementSelection selection : row.getTableView().getItems()) {
            if(!selectedOne.equals(selection)) {
                selection.setSelected(false);
            }
        }
    }

    private void createChoiceBoxIdentTypeDeaktivated(TableColumn<ElementIdentCorrelation, String> column) {
        column.setCellFactory(col -> {
            TableCell<ElementIdentCorrelation, String> cell = new TableCell<>();
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
    }

    private void createChoiceBoxIdentTypeTable(TableColumn<ElementIdentCorrelation, String> column) {
        column.setCellFactory(col -> {
            TableCell<ElementIdentCorrelation, String> cell = new TableCell<>();
            ChoiceBox<IdentTypeTable> choiceBox = new ChoiceBox<>();
            choiceBox.getItems().addAll(IdentTypeTable.values());

            // update value
            choiceBox.valueProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null) {
                    cell.getTableRow().getItem().setIdentType(nv.name());
                }
            });

            // set initial value
            cell.graphicProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow() != null && cell.getTableRow().getItem() != null && cell.getTableRow().getItem().getIdentType() != null) {
                    choiceBox.setValue(IdentTypeTable.valueOf(cell.getTableRow().getItem().getIdentType()));
                }
            });

            cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(choiceBox));
            return cell;
        });
    }

//    #########################
//    Single Course/Stock section
//    #########################

    // used by Stock and Course both single and table
    @Transactional
    public void initStockSelectionTable(WebsiteElement staleElement,TableView<ElementSelection> table, boolean singleSelection) {

        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);
        prepareStockSelectionTable(table, singleSelection);
        fillStockSelectionTable(websiteElement, table);
    }

    private void prepareStockSelectionTable(TableView<ElementSelection> table, boolean singleSelection) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Bezeichnung");
        TableColumn<ElementSelection, String> stockIsinColumn = new TableColumn<>("Isin");

        createCheckBox(selectedColumn, singleSelection);

        stockNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        stockIsinColumn.setCellValueFactory(new PropertyValueFactory<>("isin"));

        table.getColumns().add(selectedColumn);
        table.getColumns().add(stockNameColumn);
        table.getColumns().add(stockIsinColumn);
    }

    private void fillStockSelectionTable(WebsiteElement websiteElement, TableView<ElementSelection> table) {
        ArrayList<String> addedStockSelection = new ArrayList<>();

        for(ElementSelection elementSelection : websiteElement.getElementSelections()) {
            table.getItems().add(elementSelection);
            addedStockSelection.add(elementSelection.getStock().getIsin());
        }

        for(Stock stock : stockRepository.findAll()) {
            if(!addedStockSelection.contains(stock.getIsin())) {
                ElementSelection elementSelection = new ElementSelection(websiteElement, stock);
                addedStockSelection.add(stock.getIsin());
                table.getItems().add(elementSelection);
            }
        }
    }


    // used by Stock and Course
    @Transactional
    public void initCourseOrStockCorrelationTable(WebsiteElement staleElement, TableView<ElementIdentCorrelation> table , MultiplicityType multiplicityType) {
        // load anew because the element from the table has no session attached anymore and therefore can't resolve
        // lazy evaluation
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        prepareCourseOrStockCorrelationTable(table, websiteElement.getContentType(), multiplicityType);

        if(websiteElement.getContentType() == ContentType.AKTIENKURS) {
            fillCourseCorrelationTable(websiteElement,table, multiplicityType);
        } else {
            fillStockCorrelationTable(websiteElement,table, multiplicityType);
        }

    }

    private void prepareCourseOrStockCorrelationTable(TableView<ElementIdentCorrelation> table, ContentType contentType, MultiplicityType multiplicityType) {

        TableColumn<ElementIdentCorrelation, String> dataElementColumn = new TableColumn<>("Datenelement");
        TableColumn<ElementIdentCorrelation, String> identTypeColumn = new TableColumn<>("Selektionstyp");
        TableColumn<ElementIdentCorrelation, String> representationColumn = new TableColumn<>("Webseitenrepräsentation");
        representationColumn.setMinWidth(210);

        // DbColName
        if(contentType == ContentType.AKTIENKURS) {
            dataElementColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCourseDataTableColumn().getName()));
        } else {
            dataElementColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockDataTableColumn().getName()));
        }


        // choiceBox
        if(multiplicityType == MultiplicityType.EINZELWERT) {
            createChoiceBoxIdentTypeDeaktivated(identTypeColumn);
        }
        else {
            createChoiceBoxIdentTypeTable(identTypeColumn);
        }

        // representation
        representationColumn.setCellValueFactory(param -> {
            return param.getValue().representationProperty();
            //ssp.addListener((o, ov, nv) -> ssp.set(nv));
        });
        representationColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        table.getColumns().add(dataElementColumn);
        table.getColumns().add(identTypeColumn);
        table.getColumns().add(representationColumn);
    }

    private void fillStockCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> stockSelections = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection
        if(multiplicityType == MultiplicityType.EINZELWERT) addedStockColumns.add("isin");

        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            stockSelections.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getStockDataTableColumn().getName());
        }

        for(StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                stockSelections.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(stockSelections);
    }

    private void fillCourseCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> stockSelections = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection
        if(multiplicityType == MultiplicityType.EINZELWERT) addedStockColumns.add("isin");

        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            stockSelections.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getCourseDataTableColumn().getName());
        }

        for(CourseDataDbTableColumn column : courseDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                stockSelections.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(stockSelections);
    }

    // used by Stock and Course
    @Transactional
    public void saveSingleCourseOrStockSettings(WebsiteElement websiteElement) {

        for (ElementSelection selection : singleCourseOrStockSubController.getSelections()) {
            if(selection.isChanged()) elementSelectionRepository.save(selection);
        }

        elementSelectionRepository.flush();
        elementSelectionRepository.deleteAllBy_selected(false);


        for (ElementIdentCorrelation correlation : singleCourseOrStockSubController.getDbCorrelations()) {
            if(correlation.isChanged()) elementIdentCorrelationRepository.save(correlation);
        }

        websiteElementRepository.save(websiteElement );
    }

//    #########################
//    Single Exchange section
//    #########################

    public void initExchangeSelectionTable(WebsiteElement staleElement,TableView<ElementSelection> table ) {
        prepareExchangeSelectionTable(table);
        fillExchangeSelectionTable(staleElement, table);
    }

    private void prepareExchangeSelectionTable(TableView<ElementSelection> table) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Währung");

        createCheckBox(selectedColumn, true);

        stockNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().add(selectedColumn);
        table.getColumns().add(stockNameColumn);
    }

    private void fillExchangeSelectionTable(WebsiteElement websiteElement, TableView<ElementSelection> table) {
        ObservableList<ElementSelection> stockSelections = FXCollections.observableArrayList();
        ArrayList<String> addedStockSelection = new ArrayList<>();
        // hide datum column by adding it here
        addedStockSelection.add("datum");

        for(ElementSelection elementSelection : websiteElement.getElementSelections()) {
            stockSelections.add(elementSelection);
            addedStockSelection.add(elementSelection.getExchangeDataDbTableColumn().getName());
        }

        for(ExchangeDataDbTableColumn column : exchangeDataColumnRepository.findAll()) {
            if(!addedStockSelection.contains(column)) {
                ElementSelection elementSelection = new ElementSelection(websiteElement, column);

                addedStockSelection.add(column.getName());
                stockSelections.add(elementSelection);
            }
        }

        table.getItems().addAll(stockSelections);
    }

    @Transactional
    public List<ElementIdentCorrelation> initExchangeCorrelations(ChoiceBox<IdentTypeDeactivated> dateChoiceBox,
                                                                  TextField dataIdentField,
                                                                  ChoiceBox<IdentTypeDeactivated> exchangeChoiceBox,
                                                                  TextField exchangeIdentField,
                                                                  WebsiteElement staleElement) {

        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();
        List<String> added = new ArrayList<>();

        // load saved values
        for (ElementIdentCorrelation correlation : websiteElement.getElementIdentCorrelations()) {
            if(correlation.getExchangeFieldName().equals("datum")) {
                bindFieldsToCorrelation(dateChoiceBox, dataIdentField, correlation);
                added.add("datum");
            } else if(correlation.getExchangeFieldName().equals("kurs")) {
                bindFieldsToCorrelation(exchangeChoiceBox, exchangeIdentField, correlation);
                added.add("kurs");
            } else continue;
            elementIdentCorrelations.add(correlation);
        }

        // add new if not saved
        if(!added.contains("datum")) {
            var newCorrelation = new ElementIdentCorrelation(websiteElement, "datum");
            elementIdentCorrelations.add(newCorrelation);
            bindFieldsToCorrelation(dateChoiceBox, dataIdentField, newCorrelation);
        }

        // add new if not saved
        if(!added.contains("kurs")) {
            var newCorrelation = new ElementIdentCorrelation(websiteElement, "kurs");
            elementIdentCorrelations.add(newCorrelation);
            bindFieldsToCorrelation(exchangeChoiceBox, exchangeIdentField, newCorrelation);
            exchangeChoiceBox.setValue(IdentTypeDeactivated.XPATH);
        }

        return elementIdentCorrelations;
    }

    public void bindFieldsToCorrelation(ChoiceBox<IdentTypeDeactivated> choiceBox, TextInputControl textField, ElementIdentCorrelation correlation) {
        choiceBox.setValue(IdentTypeDeactivated.valueOf(correlation.getIdentType()));
        textField.setText(correlation.getRepresentation());

        textField.textProperty().addListener((o, ov, nv) -> correlation.setRepresentation(nv));

        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> correlation.setIdentType(nv.name()));
    }

    public void saveSingleExchangeSettings(WebsiteElement websiteElement) {

        for (var selection : singleExchangeSubController.getExchangeSelections()) {
            if(selection.isChanged()) {
                elementSelectionRepository.save(selection);
            }
        }

        for (var correlation : singleExchangeSubController.getElementCorrelations()) {
            if(correlation.isChanged()) {
                elementIdentCorrelationRepository.save(correlation);
            }
        }

        websiteElementRepository.save(websiteElement);
    }

//    #########################
//    Table Course section
//    #########################

    @Transactional
    public void saveTableCourseOrStockSettings(WebsiteElement websiteElement) {

        for (var selection : tableCourseSubController.getSelections()) {
            if(selection.isSelected() && selection.getElementDescCorrelation() != null) {
                elementDescCorrelationRepository.save(selection.getElementDescCorrelation());
            }
            if(selection.isChanged()) {
                elementSelectionRepository.save(selection);
            }
        }

        for (var identCorrelation : tableCourseSubController.getDbCorrelations()) {
            if(identCorrelation.isChanged()) {
                elementIdentCorrelationRepository.save(identCorrelation);
            }
        }

        elementSelectionRepository.deleteAllBy_selected(false);
        //elementDescCorrelationRepository.flush();
        websiteElementRepository.save(websiteElement);
    }

    @Transactional
    public void initCourseDescriptionTable(WebsiteElement staleElement, TableView<ElementDescCorrelation> table) {
        var websiteElement = getFreshWebsiteElement(staleElement);
        prepareCourseDescriptionTable(table);
        fillCourseDescriptionTable(websiteElement, table);
    }

    private void prepareCourseDescriptionTable(TableView<ElementDescCorrelation> table) {

        TableColumn<ElementDescCorrelation, String> dbDescriptionCol = new TableColumn<>("Bezeichnung in DB");
        TableColumn<ElementDescCorrelation, String> dbIsinCol = new TableColumn<>("Isin in DB");
        TableColumn<ElementDescCorrelation, String> wsDescriptionCol = new TableColumn<>("Bezeichnung auf Seite");
        TableColumn<ElementDescCorrelation, String> wsIsinCol = new TableColumn<>("Isin auf Seite");


        dbDescriptionCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getDescription()));
        dbIsinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getIsin()));


        // representation
        wsDescriptionCol.setCellValueFactory(param -> {
            return param.getValue().wsDescriptionProperty();
        });
        wsDescriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());

        wsIsinCol.setCellValueFactory(param -> {
            return param.getValue().wsIsinProperty();
        });
        wsIsinCol.setCellFactory(TextFieldTableCell.forTableColumn());


        table.getColumns().add(dbDescriptionCol);
        table.getColumns().add(dbIsinCol);
        table.getColumns().add(wsDescriptionCol);
        table.getColumns().add(wsIsinCol);
    }

    private void fillCourseDescriptionTable(WebsiteElement websiteElement, TableView<ElementDescCorrelation> table) {
        for(ElementSelection selection : websiteElement.getElementSelections()) {
            ElementDescCorrelation correlation = selection.getElementDescCorrelation();
            if(correlation != null) {
                table.getItems().add(correlation);
            }
        }
    }

    private void addNewElementDescCorrelation(ElementSelection elementSelection) {

        for(ElementDescCorrelation correlation : tableCourseSubController.getElementDescCorrelations()) {
            if(correlation.getElementSelection().equals(elementSelection)) return;
        }

        if(elementSelection.getElementDescCorrelation() != null) {
            tableCourseSubController.getElementDescCorrelationTableView().getItems().add(elementSelection.getElementDescCorrelation());
            return;
        }

        var correlation = new ElementDescCorrelation(elementSelection, elementSelection.getWebsiteElement());
        elementSelection.setElementDescCorrelation(correlation);
        tableCourseSubController.getElementDescCorrelationTableView().getItems().add(correlation);
    }

    private void removeElementDescCorrelation(ElementSelection elementSelection) {
        tableCourseSubController.getElementDescCorrelations().remove(elementSelection.getElementDescCorrelation());
        elementSelection.setElementDescCorrelation(null);
    }

}
