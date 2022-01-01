package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes.IDENT_TYPE_TABLE;

/**
 * abstract manager class that is used by all subtypes of gui managers
 */
public abstract class ElementManager {

    private final static String[] EXCHANGE_COLS = {"name", "kurs"};
    private final static String[] HIDDEN_SINGLE_STOCK = {"isin", "wkn","name", "typ"};
    private final static String[] EXTRA_STOCK = {"wkn","name"};
    private final static ObservableList<String> identTypeDeactivatedObservable = getObservableList(IDENT_TYPE_TABLE);

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CourseColumnRepository courseColumnRepository;
    @Autowired
    private StockColumnRepository stockColumnRepository;
    @Autowired
    private ExchangeColumnRepository exchangeColumnRepository;
    @Autowired
    protected WebsiteElementRepository websiteElementRepository;
    @Autowired
    protected ElementSelectionRepository elementSelectionRepository;
    @Autowired
    protected ElementIdentCorrelationRepository elementIdentCorrelationRepository;

    /**
     * creates an observable list from an array. used by combo boxes inside a cell of a javafx table
     *
     * @param identTypeArray an array from {@link de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes}
     * @return the observable list
     */
    private static ObservableList<String> getObservableList(@SuppressWarnings("SameParameterValue") IdentType[] identTypeArray) {
        return FXCollections.observableList(Stream.of(identTypeArray).map(Enum::name).collect(Collectors.toList()));
    }

    /**
     * non non table website element configurations aka single element configurations allow only one selection.
     * this deselects all other selected elements inside the list.
     * @param row the row which was selected inside the table
     */
    private void deselectOther(TableColumn.CellDataFeatures<ElementSelection, Boolean> row) {
        ElementSelection selectedOne = row.getValue();
        for(ElementSelection selection : row.getTableView().getItems()) {
            if(!selectedOne.equals(selection)) {
                selection.setSelected(false);
            }
        }
    }

    /**
     * sets the setCellFactory for the column to create checkboxes
     *
     * @param column the checkbox column
     * @param singleSelection if true only one selection at a time is allowed
     */
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

    /**
     * reloads an entity with a session attached to allow lazy loading of referenced
     *
     * @param staleElement the website element without session
     * @return the website element with session
     */
    protected WebsiteElement getFreshWebsiteElement(WebsiteElement staleElement) {
        return websiteElementRepository.getById(staleElement.getId());
    }

    /**
     * prepares and fills the stock selection table based on the website configuration which contains the references
     * to the {@link ElementSelection}s
     *
     * @param staleElement the selected website configuration
     * @param table the javafx selection table
     * @param singleSelection true if it is not a table configuration
     */
    @Transactional
    public void initStockSelectionTable(WebsiteElement staleElement, TableView<ElementSelection> table, boolean singleSelection) {
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);
        prepareStockSelectionTable(table, singleSelection);
        fillStockSelectionTable(websiteElement, table);
    }

    /**
     * creates the table columns and their factories
     *
     * @param table the javafx selection table
     * @param singleSelection true if it is not a table configuration
     */
    private void prepareStockSelectionTable(TableView<ElementSelection> table, boolean singleSelection) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Bezeichnung");
        TableColumn<ElementSelection, String> stockIsinColumn = new TableColumn<>("ISIN");
        TableColumn<ElementSelection, String> stockWknColumn = new TableColumn<>("WKN");

        selectedColumn.setPrefWidth(35);
        stockNameColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        stockIsinColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.22));
        stockWknColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.22));

        createCheckBox(selectedColumn, singleSelection);

        stockNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        stockIsinColumn.setCellValueFactory(new PropertyValueFactory<>("isin"));
        stockWknColumn.setCellValueFactory(new PropertyValueFactory<>("wkn"));

        table.getColumns().add(selectedColumn);
        table.getColumns().add(stockNameColumn);
        table.getColumns().add(stockIsinColumn);
        table.getColumns().add(stockWknColumn);
    }

    /**
     * adds all {@link ElementSelection} to the table and selects the previously selected
     *
     * @param websiteElement the selected website configuration
     * @param table the javafx selection table
     */
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


    /**
     * prepares and fills the stock identification correlation table (xpath/css) based on the website configuration
     * which contains the references to the {@link ElementIdentCorrelation}s
     *
     * @param staleElement the selected website configuration
     * @param table the javafx selection table
     * @param multiplicityType the {@link MultiplicityType} wo table or single
     */
    @Transactional
    public void initIdentCorrelationTable(WebsiteElement staleElement, TableView<ElementIdentCorrelation> table , MultiplicityType multiplicityType) {
        // load anew because the element from the table has no session attached anymore and therefore can't resolve
        // lazy evaluation
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        prepareIdentCorrelationTable(table);

        var type = websiteElement.getContentType();
        if(type == ContentType.AKTIENKURS) {
            fillCourseIdentCorrelationTable(websiteElement,table, multiplicityType);
        } else if(type == ContentType.STAMMDATEN) {
            fillStockIdentCorrelationTable(websiteElement,table, multiplicityType);
        } else if(type == ContentType.WECHSELKURS) {
            fillExchangeIdentCorrelationTable(websiteElement, table);
        }
    }

    /**
     * creates all the columns for the correlation table
     *
     * @param table the javafx correlation table
     */
    private void prepareIdentCorrelationTable(TableView<ElementIdentCorrelation> table) {

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
        identTypeColumn.setCellValueFactory(param -> param.getValue().identTypeProperty());
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

    /**
     * adds all identification correlation rows for all columns of the stock database table ({@link StockTableManager#TABLE_NAME}).
     * previously saved ones are added first
     *
     * @param websiteElement the website element configuration
     * @param table the javafx selection table
     * @param multiplicityType the {@link MultiplicityType} wo table or single
     */
    private void fillStockIdentCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> stockCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection nor datum
        if(multiplicityType == MultiplicityType.EINZELWERT) {
            addedStockColumns.addAll(List.of(HIDDEN_SINGLE_STOCK));
        }
        // don't need that. today's date is taken
        addedStockColumns.add("datum");

        // already saved idents
        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            stockCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getDbColName());
        }

        // idents that have no representation in the stock data table because they are from the stock table
        for(String column : List.of(EXTRA_STOCK)) {
            if(!addedStockColumns.contains(column)) {
                addedStockColumns.add(column);
                stockCorrelations.add(new ElementIdentCorrelation(
                        websiteElement, ColumnDatatype.TEXT, StockTableManager.TABLE_NAME ,column));
            }
        }

        // idents representing columns without data assigned atm
        for(StockColumn column : stockColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                stockCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(stockCorrelations);
    }

    /**
     * adds all identification correlation rows for all columns of the course database table ({@link CourseTableManager#TABLE_NAME}).
     * previously saved ones are added first
     *
     * @param websiteElement the website element configuration
     * @param table the javafx selection table
     * @param multiplicityType the {@link MultiplicityType} wo table or single
     */
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


        if(multiplicityType == MultiplicityType.TABELLE) {
            // add these for scraping identification purposes
            for(String column : List.of(EXTRA_STOCK)) {
                if(!addedStockColumns.contains(column)) {
                    addedStockColumns.add(column);
                    courseCorrelations.add(new ElementIdentCorrelation(
                            websiteElement, ColumnDatatype.TEXT, CourseTableManager.TABLE_NAME ,column));
                }
            }
        }


        for(CourseColumn column : courseColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                courseCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(courseCorrelations);
    }

    /**
     * adds all identification correlation rows for all columns of the exchange database table ({@link ExchangeTableManager#TABLE_NAME}).
     * previously saved ones are added first
     *
     * @param websiteElement the website element configuration
     * @param table the javafx selection table
     */
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
                exchangeCorrelations.add(new ElementIdentCorrelation(
                        websiteElement, ColumnDatatype.DOUBLE, ExchangeTableManager.TABLE_NAME, column));
            }
        }

        table.getItems().addAll(exchangeCorrelations);
    }


    /**
     * prepares and fills the exchange selection table based on the website configuration which contains the references
     * to the {@link ElementSelection}s.
     * the difference to the stock selection lies in the fact that every exchange pair has its own column in the exchange
     * database table ({@link ExchangeTableManager#TABLE_NAME}) and is not en entity in the table itself (like stock in the stock table)
     *
     * @param staleElement the selected website configuration
     * @param table the javafx selection table
     * @param singleSelection defines if only one checkbox of the selection table can be checked
     */
    @Transactional
    public void initExchangeSelectionTable(WebsiteElement staleElement, TableView<ElementSelection> table, boolean singleSelection) {
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);
        prepareExchangeSelectionTable(table, singleSelection);
        fillExchangeSelectionTable(websiteElement, table);
    }

    /**
     * creates the table columns and their factories
     *
     * @param table the javafx selection table
     * @param singleSelection defines if only one checkbox of the selection table can be checked
     */
    private void prepareExchangeSelectionTable(TableView<ElementSelection> table, boolean singleSelection) {
        TableColumn<ElementSelection, Boolean> selectedColumn = new TableColumn<>("Selektion");
        TableColumn<ElementSelection, String> stockNameColumn = new TableColumn<>("Währung");

        createCheckBox(selectedColumn, singleSelection);

        stockNameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().add(selectedColumn);
        table.getColumns().add(stockNameColumn);
    }

    /**
     * adds all {@link ElementSelection} to the table and selects the previously selected
     *
     * @param websiteElement the website element configuration
     * @param table the javafx selection table
     */
    private void fillExchangeSelectionTable(WebsiteElement websiteElement, TableView<ElementSelection> table) {
        ObservableList<ElementSelection> stockSelections = FXCollections.observableArrayList();
        ArrayList<String> addedStockSelection = new ArrayList<>();
        // hide datum column by adding it here
        addedStockSelection.add("datum");

        for(ElementSelection elementSelection : websiteElement.getElementSelections()) {
            stockSelections.add(elementSelection);
            addedStockSelection.add(elementSelection.getDescription());
        }

        for(ExchangeColumn column : exchangeColumnRepository.findAll()) {
            if(!addedStockSelection.contains(column.getName())) {
                ElementSelection elementSelection = new ElementSelection(websiteElement, column);

                addedStockSelection.add(column.getName());
                stockSelections.add(elementSelection);
            }
        }

        table.getItems().addAll(stockSelections);
    }


    /**
     * loads the submenu for the specific type of the configuration
     *
     * @param controllerClass the controller which will be used by the submenu
     * @param resource the file path
     * @param control some element as reference
     */
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

    /**
     * resets the given input inside the website element to the database data
     *
     * @param urlField the javafx url field
     * @param choiceBox the javafx website configuration choice box
     * @param staleElement the selected element from the javafx selection list
     */
    @Transactional
    public void resetElementRepresentation(TextField urlField, ChoiceBox<Website> choiceBox, WebsiteElement staleElement) {
        var newElement = getFreshWebsiteElement(staleElement);
        staleElement.setTableIdent(newElement.getTableIdent());
        staleElement.setTableIdenType(newElement.getTableIdenType());
        urlField.setText(newElement.getInformationUrl());
        choiceBox.setValue(newElement.getWebsite());
    }


    public WebsiteElement createNewElement(String description, ContentType contentType, MultiplicityType multiplicityType) {
        WebsiteElement element = new WebsiteElement(description, contentType, multiplicityType);
        websiteElementRepository.save(element);
        return element;
    }

    public void deleteSpecificElement(WebsiteElement element) {
        element.setElementSelections(new ArrayList<>());
        element.setElementCorrelations(new ArrayList<>());
        websiteElementRepository.delete(element);
    }


    public ObservableList<WebsiteElement> initWebsiteElementList(ListView<WebsiteElement> elementListView) {
        ObservableList<WebsiteElement> elementObservableList = FXCollections.observableList(websiteElementRepository.findAll());
        elementListView.setItems(elementObservableList);
        return elementObservableList;
    }


    public List<WebsiteElement> getElements() {
        return websiteElementRepository.findAll();
    }

}
