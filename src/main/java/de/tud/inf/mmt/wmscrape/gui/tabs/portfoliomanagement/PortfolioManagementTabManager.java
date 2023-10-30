package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PortfolioManagementTabManager {
    public TabPane portfolioManagementTabpane;

    private PortfolioManagementTabController portfolioController;

    // static data to simulate Portfolios, etc.
    public  String[] depotList = {"Depot 1", "Depot 2", "Depot 3"};
    public  String[] portfolioList = {"Portfolio 1", "Portfolio 2"};
    public  String[] kontoList = {"Konto 1", "Konto 2"};
    public  String[] ownerList = {"Inhaber 1", "Inhaber 2"};
    public  String[] depotsOfPortfolio1List = {"Depot 1", "Depot 2"};
    public  String[] kontosOfPortfolio1List = {"Konto 1", "Konto 2"};

    //
    private List<String> currentlyDisplayedElements = new ArrayList<>();
    private List<String> typesOfCurrentlyDisplayedElements = new ArrayList<>();

    public void setPortfolioController(PortfolioManagementTabController controller) {
        this.portfolioController = controller;
    }

    public List<String> getCurrentlyDisplayedElement() {
        return  currentlyDisplayedElements;
    }

    public void setCurrentlyDisplayedElement(String newElement) {
        currentlyDisplayedElements.clear();
        currentlyDisplayedElements.add(newElement);
        changeBreadcrumbs();
    }
    public void addCurrentlyDisplayedElement(String newElement) {
        currentlyDisplayedElements.add(newElement);
        changeBreadcrumbs();
    }

    public List<String> getTypeOfCurrentlyDisplayedElement() {
        return  typesOfCurrentlyDisplayedElements;
    }

    public void setTypeOfCurrentlyDisplayedElement(String newType) {
        typesOfCurrentlyDisplayedElements.clear();
        typesOfCurrentlyDisplayedElements.add(newType);
    }
    public void addTypeOfCurrentlyDisplayedElement(String newType) {
        typesOfCurrentlyDisplayedElements.add(newType);
    }

    public void showPortfolioManagementTabs() {
        if (portfolioController != null) {
            portfolioController.showPortfolioManagementTabs();
        }
    }

    public void showDepotTabs() {
        if (portfolioController != null) {
            portfolioController.showDepotTabs();
        }
    }

    public void showDepotPlanungTabs() {
        if (portfolioController != null) {
            portfolioController.showDepotPlanungTabs();
        }
    }

    public void showPortfolioTabs() {
        if (portfolioController != null) {
            portfolioController.showPortfolioTabs();
        }
    }

    public void showKontoTabs() {
        if (portfolioController != null) {
            portfolioController.showKontoTabs();
        }
    }

    public void showInhaberTabs() {
        if (portfolioController != null) {
            portfolioController.showInhaberTabs();
        }
    }
    public void removeBreadcrumbs() {
        if (portfolioController != null) {
            portfolioController.removeBreadcrumbs();
        }
    }

    public void changeBreadcrumbs() {
        if (portfolioController != null) {
            portfolioController.changeBreadcrumbs(typesOfCurrentlyDisplayedElements, currentlyDisplayedElements);
        }
    }
}
