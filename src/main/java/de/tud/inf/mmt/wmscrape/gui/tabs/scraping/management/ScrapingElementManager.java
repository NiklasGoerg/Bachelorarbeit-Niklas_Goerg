package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.stock.StockDataDbTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.Stock;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.StockRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElementRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelectionRepository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes.IDENT_TYPE_DEACTIVATED;
import static de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypes.IDENT_TYPE_TABLE;

public abstract class ScrapingElementManager {

    private final static String[] EXCHANGE_COLS = {"bezeichnung", "kurs"};
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
                }
            });
            return sbp;
        });
    }

    private void createChoiceBoxIdentType(TableColumn<ElementIdentCorrelation, String> column, MultiplicityType multiplicityType) {
        column.setCellFactory(col -> {
            TableCell<ElementIdentCorrelation, String> cell = new TableCell<>();
            ChoiceBox<IdentType> choiceBox = new ChoiceBox<>();

            if(multiplicityType == MultiplicityType.EINZELWERT) {
                choiceBox.getItems().addAll(IDENT_TYPE_DEACTIVATED);
            } else {
                choiceBox.getItems().addAll(IDENT_TYPE_TABLE);
            }

            // update value
            choiceBox.valueProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow().getItem() != null) {
                    cell.getTableRow().getItem().setIdentType(nv.name());
                }
            });

            // set initial value
            cell.graphicProperty().addListener((o, ov, nv) -> {
                if (cell.getTableRow() != null && cell.getTableRow().getItem() != null && cell.getTableRow().getItem().getIdentTypeName() != null) {
                    choiceBox.setValue(IdentType.valueOf(cell.getTableRow().getItem().getIdentTypeName()));
                }
            });

            cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(choiceBox));
            return cell;
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


    @Transactional
    public void initCorrelationTable(WebsiteElement staleElement, TableView<ElementIdentCorrelation> table , MultiplicityType multiplicityType) {
        // load anew because the element from the table has no session attached anymore and therefore can't resolve
        // lazy evaluation
        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        prepareCorrelationTable(table, websiteElement.getContentType(), multiplicityType);

        var type = websiteElement.getContentType();
        if(type == ContentType.AKTIENKURS) {
            fillCourseCorrelationTable(websiteElement,table, multiplicityType);
        } else if(type == ContentType.STAMMDATEN) {
            fillStockCorrelationTable(websiteElement,table, multiplicityType);
        } else if(type == ContentType.WECHSELKURS) {
            fillExchangeCorrelationTable(websiteElement, table);
        }
    }

    private void prepareCorrelationTable(TableView<ElementIdentCorrelation> table, ContentType contentType, MultiplicityType multiplicityType) {

        TableColumn<ElementIdentCorrelation, String> dataElementColumn = new TableColumn<>("Datenelement");
        TableColumn<ElementIdentCorrelation, String> identTypeColumn = new TableColumn<>("Selektionstyp");
        TableColumn<ElementIdentCorrelation, String> representationColumn = new TableColumn<>("Webseitenrepräsentation");
        representationColumn.setMinWidth(210);

        // DbColName
        if(contentType == ContentType.AKTIENKURS) {
            dataElementColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCourseDataDbTableColumn().getName()));
        } else if(contentType == ContentType.STAMMDATEN){
            dataElementColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getStockDataTableColumn().getName()));
        } else  {
            dataElementColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getExchangeFieldName()));
        }

        // choiceBox
        createChoiceBoxIdentType(identTypeColumn, multiplicityType);

        // representation
        representationColumn.setCellValueFactory(param -> param.getValue().representationProperty());
        representationColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        table.getColumns().add(dataElementColumn);
        table.getColumns().add(identTypeColumn);
        table.getColumns().add(representationColumn);
    }

    private void fillStockCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> stockCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection nor datum
        if(multiplicityType == MultiplicityType.EINZELWERT) addedStockColumns.add("isin");
        addedStockColumns.add("datum");

        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            stockCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getStockDataTableColumn().getName());
        }

        for(StockDataDbTableColumn column : stockDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                stockCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(stockCorrelations);
    }

    private void fillCourseCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table, MultiplicityType multiplicityType) {
        ObservableList<ElementIdentCorrelation> courseCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();

        // don't need isin, it's defined in selection nor datum
        if(multiplicityType == MultiplicityType.EINZELWERT) addedStockColumns.add("isin");
        addedStockColumns.add("datum");

        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            courseCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getCourseDataDbTableColumn().getName());
        }

        for(CourseDataDbTableColumn column : courseDataColumnRepository.findAll()) {
            if(!addedStockColumns.contains(column.getName())) {
                addedStockColumns.add(column.getName());
                courseCorrelations.add(new ElementIdentCorrelation(websiteElement, column));
            }
        }

        table.getItems().addAll(courseCorrelations);
    }

    private void fillExchangeCorrelationTable(WebsiteElement websiteElement, TableView<ElementIdentCorrelation> table) {
        ObservableList<ElementIdentCorrelation> exchangeCorrelations = FXCollections.observableArrayList();
        ArrayList<String> addedStockColumns = new ArrayList<>();


        for (ElementIdentCorrelation elementIdentCorrelation : websiteElement.getElementIdentCorrelations()) {
            exchangeCorrelations.add(elementIdentCorrelation);
            addedStockColumns.add(elementIdentCorrelation.getExchangeFieldName());
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
