package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.gui;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.exchange.ExchangeTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleCourseOrStockSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleExchangeSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElementManagerCourseAndExchange extends ElementManager {

    @Autowired
    private SingleExchangeSubController singleExchangeSubController;
    @Autowired
    private SingleCourseOrStockSubController singleCourseOrStockSubController;

    /**
     * used by the {@link SingleExchangeSubController} to initialize the correlations. the only submenu that has no
     * correlation table because there is only the price that has to be identified.
     *
     * @param exchangeChoiceBox the choicebox containing the ident types ({@link IdentType})
     * @param exchangeIdentField the text field containg the identifier (e.g. xpath or css)
     * @param regexField the regex string used for substring selection
     * @param staleElement the website element configuration
     * @return only the price correlation inside a list
     */
    @Transactional
    public List<ElementIdentCorrelation> initExchangeIdentCorrelations(ChoiceBox<IdentType> exchangeChoiceBox,
                                                                       TextField exchangeIdentField, TextField regexField,
                                                                       WebsiteElement staleElement) {

        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();
        List<String> added = new ArrayList<>();

        // load saved values
        for (ElementIdentCorrelation correlation : websiteElement.getElementIdentCorrelations()) {
            if(correlation.getDbColName().equals("kurs")) {
                bindExchangeFieldsToCorrelation(exchangeChoiceBox, exchangeIdentField, regexField, correlation);
                added.add("kurs");
            } else continue;
            elementIdentCorrelations.add(correlation);
        }

        // add new if not saved
        if(!added.contains("kurs")) {
            var newCorrelation = new ElementIdentCorrelation(websiteElement, ColumnDatatype.DOUBLE, ExchangeTableManager.TABLE_NAME,"kurs");
            elementIdentCorrelations.add(newCorrelation);
            bindExchangeFieldsToCorrelation(exchangeChoiceBox, exchangeIdentField, regexField, newCorrelation);
            exchangeChoiceBox.setValue(IdentType.XPATH);
        }

        return elementIdentCorrelations;
    }

    /**
     * adds the listeners to the fields to detect changes.
     *
     * @param choiceBox the choicebox containing the ident types ({@link IdentType})
     * @param identField the text field containg the identifier (e.g. xpath or css)
     * @param regexField the regex string used for substring selection
     * @param correlation the price correlation element which stores the correlation
     */
    public void bindExchangeFieldsToCorrelation(ChoiceBox<IdentType> choiceBox, TextField identField, TextField regexField, ElementIdentCorrelation correlation) {
        choiceBox.setValue(correlation.getIdentType());
        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> correlation.setIdentType(nv.name()));

        identField.textProperty().bindBidirectional(correlation.identificationProperty());
        regexField.textProperty().bindBidirectional(correlation.regexProperty());

    }

    @Transactional
    public void saveSingleCourseOrStockSettings(WebsiteElement websiteElement) {
        saveSingleSettings(websiteElement,
                singleCourseOrStockSubController.getSelections(),
                singleCourseOrStockSubController.getDbCorrelations());
    }

    @Transactional
    public void saveSingleExchangeSettings(WebsiteElement websiteElement) {
        saveSingleSettings(websiteElement,
                singleExchangeSubController.getExchangeSelections(),
                singleExchangeSubController.getElementCorrelations());

    }

    /**
     * stores all the input for the current website element configuration. the flush is necessary
     * otherwise database restraints could fail.
     *
     * <li>1. selections</li>
     * <li>2. correlations</li>
     * <li>3. the element configuration</li>
     *
     * @param element the website element configuration
     * @param selections all selection elements from the selection table
     * @param correlations the correlations for the columns
     */
    public void saveSingleSettings(WebsiteElement element, List<ElementSelection> selections, List<ElementIdentCorrelation> correlations) {
        websiteElementRepository.save(element);

        for (var selection : selections) {
            if(selection.isChanged()) elementSelectionRepository.save(selection);
        }

        elementSelectionRepository.flush();
        elementSelectionRepository.deleteAllBy_selected(false);


        for (ElementIdentCorrelation correlation : correlations) {
            if(correlation.isChanged()) elementIdentCorrelationRepository.save(correlation);
        }

    }


    @Override
    protected void removeElementDescCorrelation(ElementSelection value) {
        throw new NotImplementedFunctionException("This Method is only implemented in "+ ElementManagerTable.class);
    }

    @Override
    protected void addNewElementDescCorrelation(ElementSelection value) {
        throw new NotImplementedFunctionException("This Method is only implemented in "+ ElementManagerTable.class);
    }
}
