package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.TableSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementDescCorrelationRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScrapingTableManager extends ScrapingElementManager {

    @Autowired
    private ElementDescCorrelationRepository elementDescCorrelationRepository;
    @Autowired
    private TableSubController tableSubController;

    @Transactional
    public void initCourseOrStockDescriptionTable(WebsiteElement staleElement, TableView<ElementDescCorrelation> table) {
        var websiteElement = getFreshWebsiteElement(staleElement);
        prepareCourseDescriptionTable(table);
        fillDescriptionTable(websiteElement, table);
    }

    @Transactional
    public void initExchangeDescriptionTable(WebsiteElement staleElement, TableView<ElementDescCorrelation> table) {
        var websiteElement = getFreshWebsiteElement(staleElement);
        prepareExchangeDescriptionTable(table);
        fillDescriptionTable(websiteElement, table);
    }

    private void prepareCourseDescriptionTable(TableView<ElementDescCorrelation> table) {

        TableColumn<ElementDescCorrelation, String> dbDescriptionCol = new TableColumn<>("Bezeichnung in DB");
        TableColumn<ElementDescCorrelation, String> dbIsinCol = new TableColumn<>("Isin in DB");
        TableColumn<ElementDescCorrelation, String> wsDescriptionCol = new TableColumn<>("Bezeichnung auf der Seite");
        TableColumn<ElementDescCorrelation, String> wsIsinCol = new TableColumn<>("Isin auf Seite");


        dbDescriptionCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getDescription()));
        dbIsinCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getIsin()));


        // representation
        wsDescriptionCol.setCellValueFactory(param -> param.getValue().wsDescriptionProperty());
        wsDescriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());

        wsIsinCol.setCellValueFactory(param -> param.getValue().wsIsinProperty());
        wsIsinCol.setCellFactory(TextFieldTableCell.forTableColumn());


        table.getColumns().add(dbIsinCol);
        table.getColumns().add(wsIsinCol);
        table.getColumns().add(dbDescriptionCol);
        table.getColumns().add(wsDescriptionCol);
    }

    private void prepareExchangeDescriptionTable(TableView<ElementDescCorrelation> table) {

        TableColumn<ElementDescCorrelation, String> dbDescriptionCol = new TableColumn<>("Bezeichnung in DB");
        TableColumn<ElementDescCorrelation, String> wsDescriptionCol = new TableColumn<>("Bezeichnung auf der Seite");

        dbDescriptionCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getElementSelection().getDescription()));

        // representation
        wsDescriptionCol.setCellValueFactory(param -> param.getValue().wsCurrencyNameProperty());
        wsDescriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());


        table.getColumns().add(dbDescriptionCol);
        table.getColumns().add(wsDescriptionCol);
    }

    private void fillDescriptionTable(WebsiteElement websiteElement, TableView<ElementDescCorrelation> table) {
        for (ElementSelection selection : websiteElement.getElementSelections()) {
            ElementDescCorrelation correlation = selection.getElementDescCorrelation();
            if (correlation != null) {
                table.getItems().add(correlation);
            } else if (selection.isSelected()) {
                addNewElementDescCorrelation(selection);
            }
        }
    }

    protected void addNewElementDescCorrelation(ElementSelection elementSelection) {

        for(ElementDescCorrelation correlation : tableSubController.getElementDescCorrelations()) {
            if(correlation.getElementSelection().equals(elementSelection)) {
                return;
            }
        }

        if(elementSelection.getElementDescCorrelation() != null) {
            tableSubController.getElementDescCorrelations().add(elementSelection.getElementDescCorrelation());
            return;
        }

        var correlation = new ElementDescCorrelation(elementSelection, elementSelection.getWebsiteElement());
        elementSelection.setElementDescCorrelation(correlation);
        tableSubController.getElementDescCorrelations().add(correlation);
    }

    protected void removeElementDescCorrelation(ElementSelection elementSelection) {
        ElementDescCorrelation correlation = elementSelection.getElementDescCorrelation();
        if (correlation == null) return;
        tableSubController.getElementDescCorrelations().remove(correlation);
        elementSelection.setElementDescCorrelation(null);
    }

    @Transactional
    public void saveTableSettings(WebsiteElement websiteElement) {

        for(var correlation : tableSubController.getElementDescCorrelations()) {
            if(correlation.isChanged()) {
                elementDescCorrelationRepository.save(correlation);
            }
        }

        for (var selection : tableSubController.getSelections()) {
            if(selection.isChanged()) {
                elementSelectionRepository.save(selection);
            }
        }

        elementDescCorrelationRepository.flush();
        elementSelectionRepository.flush();

        for (var identCorrelation : tableSubController.getDbCorrelations()) {
            if(identCorrelation.isChanged()) {
                elementIdentCorrelationRepository.save(identCorrelation);
            }
        }

        elementSelectionRepository.deleteAllBy_selected(false);
        websiteElementRepository.save(websiteElement);
    }

}
