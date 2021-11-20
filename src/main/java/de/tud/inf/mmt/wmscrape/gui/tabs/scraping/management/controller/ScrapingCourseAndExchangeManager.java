package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleCourseOrStockSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.element.SingleExchangeSubController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingCourseAndExchangeManager extends ScrapingElementManager {

    @Autowired
    private SingleExchangeSubController singleExchangeSubController;
    @Autowired
    private SingleCourseOrStockSubController singleCourseOrStockSubController;

    @Transactional
    public List<ElementIdentCorrelation> initExchangeCorrelations(ChoiceBox<IdentType> exchangeChoiceBox,
                                                                  TextField exchangeIdentField,
                                                                  WebsiteElement staleElement) {

        WebsiteElement websiteElement = getFreshWebsiteElement(staleElement);

        List<ElementIdentCorrelation> elementIdentCorrelations = new ArrayList<>();
        List<String> added = new ArrayList<>();

        // load saved values
        for (ElementIdentCorrelation correlation : websiteElement.getElementIdentCorrelations()) {
            if(correlation.getDbColName().equals("kurs")) {
                bindExchangeFieldsToCorrelation(exchangeChoiceBox, exchangeIdentField, correlation);
                added.add("kurs");
            } else continue;
            elementIdentCorrelations.add(correlation);
        }

        // add new if not saved
        if(!added.contains("kurs")) {
            var newCorrelation = new ElementIdentCorrelation(websiteElement, "kurs");
            elementIdentCorrelations.add(newCorrelation);
            bindExchangeFieldsToCorrelation(exchangeChoiceBox, exchangeIdentField, newCorrelation);
            exchangeChoiceBox.setValue(IdentType.XPATH);
        }

        return elementIdentCorrelations;
    }

    public void bindExchangeFieldsToCorrelation(ChoiceBox<IdentType> choiceBox, TextInputControl textField, ElementIdentCorrelation correlation) {
        choiceBox.setValue(correlation.getIdentType());
        textField.setText(correlation.getIdentification());

        textField.textProperty().addListener((o, ov, nv) -> correlation.setIdentification(nv));

        choiceBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> correlation.setIdentType(nv.name()));
    }

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


    @Override
    protected void removeElementDescCorrelation(ElementSelection value) {
        throw new IllegalCallerException("This Method is only implemented in "+ScrapingTableManager.class);
    }

    @Override
    protected void addNewElementDescCorrelation(ElementSelection value) {
        throw new IllegalCallerException("This Method is only implemented in "+ScrapingTableManager.class);
    }
}
