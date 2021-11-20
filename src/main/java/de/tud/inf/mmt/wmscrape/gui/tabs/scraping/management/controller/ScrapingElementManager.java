package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.controller;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElementRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.MultiplicityType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelectionRepository;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes.IDENT_TYPE_DEACTIVATED;

public abstract class ScrapingElementManager {

    private final static String[] EXCHANGE_COLS = {"bezeichnung", "kurs"};
    private final static ObservableList<String> identTypeDeactivatedObservable = getObservableList(IDENT_TYPE_DEACTIVATED);

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CourseDataColumnRepository courseDataColumnRepository;
    @Autowired
    private StockDataColumnRepository stockDataColumnRepository;
    @Autowired
    private ExchangeDataColumnRepository exchangeDataColumnRepository;
    @Autowired
    protected WebsiteElementRepository websiteElementRepository;
    @Autowired
    protected ElementSelectionRepository elementSelectionRepository;
    @Autowired
    protected ElementIdentCorrelationRepository elementIdentCorrelationRepository;

    private static ObservableList<String> getObservableList(IdentType[] identTypeArray) {
        return FXCollections.observableList(Stream.of(identTypeArray).map(Enum::name).collect(Collectors.toList()));
    }

    private void deselectOther(TableColumn.CellDataFeatures<ElementSelection, Boolean> row) {
        ElementSelection selectedOne = row.getValue();
        for(ElementSelection selection : row.getTableView().getItems()) {
            if(!selectedOne.equals(selection)) {
                selection.setSelected(false);
            }
        }
    }

    protected void createCheckBox(TableColumn<ElementSelection, Boolean> column, boolean singleSelection) {
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setCellValueFactory(row -> {
            SimpleBooleanProperty sbp = row.getValue().selectedProperty();
            sbp.addListener( (o, ov, nv) -> {
                if(singleSelection) {
                    if(nv) deselectOther(row);
                } else {
                    if(nv) addNewElementDescCorrelation(row.getValue());
                    else removeElementDescCorrelation(row.getValue());
                    column.getTableView().refresh();
                }
            });
            return sbp;
        });
    }

    protected abstract void removeElementDescCorrelation(ElementSelection value);

    protected abstract void addNewElementDescCorrelation(ElementSelection value);

    protected WebsiteElement getFreshWebsiteElement(WebsiteElement staleElement) {
        return websiteElementRepository.getById(staleElement.getId());
    }


    @Transactional
    public void initStockSelectionTable(WebsiteElement staleElement, TableView<ElementSelection> table, boolean singleSelection) {
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);
        prepareStockSelectionTable(table, singleSelection);
        fillStockSelectionTable(websiteElement, table);
    }

    private void prepareStockSelectionTable(TableView<ElementSelection> table, boolean singleSelection) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Bezeichnung");
        TableColumn<ElementSelection, String> stockIsinColumn = new TableColumn<>("Isin");

        selectedColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.12));
        stockNameColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.6));
        stockIsinColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.22));

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
            addedStockSelection.add(elementSelection.getIsin());
        }

        for(Stock stock : stockRepository.findAll()) {
            if(!addedStockSelection.contains(stock.getIsin())) {
                ElementSelection elementSelection = new ElementSelection(websiteElement, stock);
                addedStockSelection.add(stock.getIsin());
                table.getItems().add(elementSelection);
            }
        }
    }


    @Transactional
    public void initIdentCorrelationTable(WebsiteElement staleElement, TableView<ElementIdentCorrelation> table , MultiplicityType multiplicityType) {
        // load anew because the element from the table has no session attached anymore and therefore can't resolve
        // lazy evaluation
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        prepareCorrelationTable(table);

        var type = websiteElement.getContentType();
        if(type == ContentType.AKTIENKURS) {
            fillCourseIdentCorrelationTable(websiteElement,table, multiplicityType);
        } else if(type == ContentType.STAMMDATEN) {
            fillStockIdentCorrelationTable(websiteElement,table, multiplicityType);
        } else if(type == ContentType.WECHSELKURS) {
            fillExchangeIdentCorrelationTable(websiteElement, table);
        }
    }

    private void prepareCorrelationTable(TableView<ElementIdentCorrelation> table) {

        TableColumn<ElementIdentCorrelation, String> nameColumn = new TableColumn<>("Datenelement");
        TableColumn<ElementIdentCorrelation, String> typeColumn = new TableColumn<>("DB Datentyp");
        TableColumn<ElementIdentCorrelation, String> identTypeColumn = new TableColumn<>("Selektionstyp");
        TableColumn<ElementIdentCorrelation, String> identificationColumn = new TableColumn<>("Webseitenidentifikation");
        TableColumn<ElementIdentCorrelation, String> regexColumn = new TableColumn<>("Regex-Unterauswahl");

        nameColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.14));
        typeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.14));
        identTypeColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        identificationColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.275));
        regexColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.23));


        identificationColumn.setMinWidth(200);
        identTypeColumn.setMinWidth(135);


        // DbColName
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDbColName()));
        // datatype
        typeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getColumnDatatype().name()));
        // choiceBox
        identTypeColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(identTypeDeactivatedObservable));


        // representation
        identificationColumn.setCellValueFactory(param -> param.getValue().identificationProperty());
        identificationColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // regex
        regexColumn.setCellValueFactory(param -> param.getValue().regexProperty());
        regexColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        table.getColumns().add(nameColumn);
        table.getColumns().add(typeColumn);
        table.getColumns().add(identTypeColumn);
        table.getColumns().add(identificationColumn);
        table.getColumns().add(regexColumn);
    }

    private void fillStockIdentCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> stockCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection nor datum
        if(multiplicityType == MultiplicityType.EINZELWERT) addedStockColumns.add("isin");
        addedStockColumns.add("datum");

        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            stockCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getDbColName());
        }

        for(StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                stockCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(stockCorrelations);
    }

    private void fillCourseIdentCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> courseCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection nor datum
        if(multiplicityType == MultiplicityType.EINZELWERT) addedStockColumns.add("isin");
        addedStockColumns.add("datum");

        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            courseCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getDbColName());
        }

        for(CourseDataDbTableColumn column : courseDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                courseCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(courseCorrelations);
    }

    private void fillExchangeIdentCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table) {
        ObservableList<ElementIdentCorrelation> exchangeCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();


        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            exchangeCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getDbColName());
        }

        for(String column : EXCHANGE_COLS) {
            if(!addedStockColumns.contains(column)) {
                addedStockColumns.add(column);
                exchangeCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(exchangeCorrelations);
    }


    @Transactional
    public void initExchangeSelectionTable(WebsiteElement staleElement, TableView<ElementSelection> table, boolean singleSelection) {
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);
        prepareExchangeSelectionTable(table, singleSelection);
        fillExchangeSelectionTable(websiteElement, table);
    }

    private void prepareExchangeSelectionTable(TableView<ElementSelection> table, boolean singleSelection) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Währung");

        createCheckBox(selectedColumn, singleSelection);

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
            if(!addedStockSelection.contains(column.getName())) {
                ElementSelection elementSelection = new ElementSelection(websiteElement, column);

                addedStockSelection.add(column.getName());
                stockSelections.add(elementSelection);
            }
        }

        table.getItems().addAll(stockSelections);
    }
}
