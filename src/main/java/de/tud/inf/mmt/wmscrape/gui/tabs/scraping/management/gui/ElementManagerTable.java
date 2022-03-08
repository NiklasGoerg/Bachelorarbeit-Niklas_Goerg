package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui;

import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.element.HistoricTableSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.TableSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.description.ElementDescCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.ContentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElementManagerTable extends ElementManager {

    @Autowired
    private ElementDescCorrelationRepository elementDescCorrelationRepository;
    @Autowired
    private TableSubController tableSubController;
    @Autowired
    private HistoricTableSubController historicTableSubController;

    /**
     * used by the {@link TableSubController} to initialize and fill the description correlations for the stock or course type.
     *
     * @param staleElement the website element configuration
     * @param table the javafx correlation table
     */
    @Transactional
    public void initCourseOrStockDescCorrelationTable(WebsiteElement staleElement, TableView<ElementDescCorrelation> table) {
        var websiteElement = getFreshWebsiteElement(staleElement);
        prepareCourseDescCorrelationTable(table);
        fillDescCorrelationTable(websiteElement, table);
    }

    /**
     * used by the {@link TableSubController} to initialize the description correlations for the exchange type.
     *
     * @param staleElement the website element configuration
     * @param table the javafx correlation table
     */
    @Transactional
    public void initExchangeDescCorrelationTable(WebsiteElement staleElement, TableView<ElementDescCorrelation> table) {
        var websiteElement = getFreshWebsiteElement(staleElement);
        prepareExchangeDescCorrelationTable(table);
        fillDescCorrelationTable(websiteElement, table);
    }

    /**
     * adds the columns to the correlation table and sets the factories
     *
     * @param table the javafx correlation table
     */
    private void prepareCourseDescCorrelationTable(TableView<ElementDescCorrelation> table) {

        TableColumn<ElementDescCorrelation, String> dbDescriptionCol = new TableColumn<>("DB-Name");
        TableColumn<ElementDescCorrelation, String> dbIsinCol = new TableColumn<>("DB-ISIN");
        TableColumn<ElementDescCorrelation, String> wsDescriptionCol = new TableColumn<>("Seite-Name");
        TableColumn<ElementDescCorrelation, String> wsIsinCol = new TableColumn<>("Seite-ISIN");
        TableColumn<ElementDescCorrelation, String> dbWknColl = new TableColumn<>("DB-WKN");
        TableColumn<ElementDescCorrelation, String> wsWknColl = new TableColumn<>("Seite-WKN");

        dbIsinCol.prefWidthProperty().bind(table.widthProperty().multiply(0.18));
        wsIsinCol.prefWidthProperty().bind(table.widthProperty().multiply(0.18));
        dbWknColl.prefWidthProperty().bind(table.widthProperty().multiply(0.14));
        wsWknColl.prefWidthProperty().bind(table.widthProperty().multiply(0.14));
        dbDescriptionCol.prefWidthProperty().bind(table.widthProperty().multiply(0.18));
        wsDescriptionCol.prefWidthProperty().bind(table.widthProperty().multiply(0.18));


        dbDescriptionCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getDescription()));
        dbIsinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getIsin()));
        dbWknColl.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getWkn()));

        // representation
        wsDescriptionCol.setCellValueFactory(param -> param.getValue().wsDescriptionProperty());
        textFieldCellFactory(wsDescriptionCol);

        wsIsinCol.setCellValueFactory(param -> param.getValue().wsIsinProperty());
        textFieldCellFactory(wsIsinCol);

        wsWknColl.setCellValueFactory(param -> param.getValue().wsWknProperty());
        textFieldCellFactory(wsWknColl);

        table.getColumns().add(dbDescriptionCol);
        table.getColumns().add(wsDescriptionCol);
        table.getColumns().add(dbIsinCol);
        table.getColumns().add(wsIsinCol);
        table.getColumns().add(dbWknColl);
        table.getColumns().add(wsWknColl);
    }

    /**
     * adds the columns to the correlation table and sets the factories
     *
     * @param table the javafx correlation table
     */
    private void prepareExchangeDescCorrelationTable(TableView<ElementDescCorrelation> table) {

        TableColumn<ElementDescCorrelation, String> dbDescriptionCol = new TableColumn<>("Bezeichnung in DB");
        TableColumn<ElementDescCorrelation, String> wsDescriptionCol = new TableColumn<>("Bezeichnung auf der Seite");

        dbDescriptionCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getDescription()));

        // representation
        wsDescriptionCol.setCellValueFactory(param -> param.getValue().wsDescriptionProperty());
        textFieldCellFactory(wsDescriptionCol);


        table.getColumns().add(dbDescriptionCol);
        table.getColumns().add(wsDescriptionCol);
    }

    /**
     * adds all the saved correlations from the website configuration element to the table and adds te missing ones
     *
     * @param websiteElement the current website element configuration
     * @param table the javafx description correlation table
     */
    private void fillDescCorrelationTable(WebsiteElement websiteElement, TableView<ElementDescCorrelation> table) {
        for (ElementSelection selection : websiteElement.getElementSelections()) {
            ElementDescCorrelation correlation = selection.getElementDescCorrelation();
            if (correlation != null) {
                table.getItems().add(correlation);
            } else if (selection.isSelected()) {
                addNewElementDescCorrelation(selection);
            }
        }
    }

    /**
     * adds a new description correlation if an element has be selected inside the selection table
     *
     * @param elementSelection the selected element
     */
    protected void addNewElementDescCorrelation(ElementSelection elementSelection) {
        var correlations = GetElementDescCorrelations(elementSelection.getWebsiteElement());

        for(ElementDescCorrelation correlation : correlations) {
            if(correlation.getElementSelection().equals(elementSelection)) {
                return;
            }
        }

        if(elementSelection.getElementDescCorrelation() != null) {
            correlations.add(elementSelection.getElementDescCorrelation());
            return;
        }

        var correlation = new ElementDescCorrelation(elementSelection);
        elementSelection.setElementDescCorrelation(correlation);
        correlations.add(correlation);
    }

    /**
     * removes a description correlation if an element has be unselected inside the selection table
     *
     * @param elementSelection the selected element
     */
    protected void removeElementDescCorrelation(ElementSelection elementSelection) {
        ElementDescCorrelation correlation = elementSelection.getElementDescCorrelation();
        if (correlation == null) return;
        GetElementDescCorrelations(elementSelection.getWebsiteElement()).remove(correlation);
        elementSelection.setElementDescCorrelation(null);
    }

    /**
     * stores all the input for the current website element configuration. the flush is necessary
     * otherwise database restraints could fail.
     *
     * <li>1. correlations</li>
     * <li>2. selections</li>
     * <li>3. the element configuration</li>
     * <li>4. removes unselected selection elements from the database</li>
     *
     * @param websiteElement the website element configuration
     */
    @Transactional
    public void saveTableSettings(WebsiteElement websiteElement) {
        websiteElementRepository.saveAndFlush(websiteElement);

        var controller = websiteElement.getContentType() == ContentType.HISTORISCH ? historicTableSubController : tableSubController;

        for (var selection : controller.getSelections()) {
            if(selection.isChanged()) {
                elementSelectionRepository.save(selection);
            }
        }
        elementSelectionRepository.flush();

        for(var correlation : GetElementDescCorrelations(websiteElement)) {
            if(correlation.isChanged()) {
                elementDescCorrelationRepository.save(correlation);
            }
        }
        elementDescCorrelationRepository.flush();

        for (var identCorrelation : controller.getDbCorrelations()) {
            if(identCorrelation.isChanged()) {
                elementIdentCorrelationRepository.save(identCorrelation);
            }
        }

        elementSelectionRepository.deleteAllBy_selected(false);
    }

    private ObservableList<ElementDescCorrelation> GetElementDescCorrelations(WebsiteElement websiteElement) {
        return websiteElement.getContentType() == ContentType.HISTORISCH ? historicTableSubController.getElementDescCorrelations() : tableSubController.getElementDescCorrelations();
    }

    private void textFieldCellFactory(TableColumn<ElementDescCorrelation, String> column) {
        column.setCellFactory(TextFieldTableCell.forTableColumn());
    }
}
