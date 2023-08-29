package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement;

import org.springframework.stereotype.Service;

@Service
public class PortfolioManagementTabManager {
    private PortfolioManagementTabController portfolioController;

    public void setPortfolioController(PortfolioManagementTabController controller) {
        this.portfolioController = controller;
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
}
