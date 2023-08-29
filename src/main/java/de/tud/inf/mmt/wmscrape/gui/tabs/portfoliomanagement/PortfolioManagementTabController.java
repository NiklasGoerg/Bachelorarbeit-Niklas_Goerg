package de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.DepotListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.depot.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.depot.planung.DepotPlanungOrderController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.depots.depot.planung.DepotPlanungWertpapiervergleichController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.kontos.KontoListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.kontos.konto.KontoOverviewController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.kontos.konto.KontoTransactionsController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners.OwnerListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners.owner.OwnerOverviewController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners.owner.OwnerVermögenController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.PortfolioListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio.PortfolioAnalyseController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio.PortfolioBenchmarkController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio.PortfolioStrukturController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class PortfolioManagementTabController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button switchSceneButton;
    @FXML
    private TabPane portfolioManagementTabPane;
    @FXML
    private Label currentUserLabel;

    @Autowired
    private PortfolioManagementTabManager portfolioManagementTabManager;
    @Autowired
    private PortfolioListController portfolioListController;
    @Autowired
    private DepotListController depotListController;
    @Autowired
    private KontoListController kontoListController;
    @Autowired
    private OwnerListController ownerListController;


    @Autowired
    private PortfolioStrukturController portfolioStrukturController;
    @Autowired
    private PortfolioAnalyseController portfolioAnalyseController;
    @Autowired
    private PortfolioBenchmarkController portfolioBenchmarkController;

    @Autowired
    private DepotPlanungController depotPlanungController;
    @Autowired
    private DepotWertpapierController depotWertpapierController;
    @Autowired
    private DepotStrukturController depotStrukturController;
    @Autowired
    private DepotTransaktionenController depotTransaktionenController;
    @Autowired
    private DepotAnlagestrategieController depotAnlagestrategieController;
    @Autowired
    private DepotPlanungWertpapiervergleichController depotPlanungWertpapiervergleichController;
    @Autowired
    private DepotPlanungOrderController depotPlanungOrderController;

    @Autowired
    private KontoOverviewController kontoOverviewController;
    @Autowired
    private KontoTransactionsController kontoTransactionsController;

    @Autowired
    private OwnerOverviewController ownerOverviewController;
    @Autowired
    private OwnerVermögenController ownerVermögenController;


    private Tab portfoliosTab;
    private Tab portfolioAnalyseTab;
    private Tab portfolioBenchmarkTab;
    private Tab portfolioStrukturTab;
    private Tab depotTab;
    private Tab depotWertpapierTab;
    private Tab depotStrukturTab;
    private Tab depotPlanungTab;
    private Tab depotPlanungVergleichTab;
    private Tab depotPlanungOrdersTab;
    private Tab depotTransaktionenTab;
    private Tab depotAnlageStrategieTab;
    private Tab kontoTab;
    private Tab kontoÜbersichtTab;
    private Tab kontoTransaktionenTab;
    private Tab inhaberTab;
    private Tab inhaberÜbersichtTab;
    private Tab inhaberVermögenTab;


    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() throws IOException {
        portfolioManagementTabManager = new PortfolioManagementTabManager();
        portfolioManagementTabManager.setPortfolioController(this);

        portfolioListController = new PortfolioListController(portfolioManagementTabManager);
        Parent parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolios.fxml", portfolioListController);
        portfoliosTab = createStyledTab("Portfolios", parent);
        portfolioManagementTabPane.getTabs().add(portfoliosTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioAnalyse.fxml", portfolioAnalyseController);
        portfolioAnalyseTab = createStyledTab("Analyse", parent);
        portfolioManagementTabPane.getTabs().add(portfolioAnalyseTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioBenchmarks.fxml", portfolioBenchmarkController);
        portfolioBenchmarkTab = createStyledTab("Benchmarks", parent);
        portfolioManagementTabPane.getTabs().add(portfolioBenchmarkTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioStruktur.fxml", portfolioStrukturController);
        portfolioStrukturTab = createStyledTab("Struktur", parent);
        portfolioManagementTabPane.getTabs().add(portfolioStrukturTab);

        depotListController = new DepotListController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depots.fxml", depotListController);
        depotTab = createStyledTab("Depots", parent);
        portfolioManagementTabPane.getTabs().add(depotTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotWertpapier.fxml", depotWertpapierController);
        depotWertpapierTab = createStyledTab("Wertpapiere", parent);
        portfolioManagementTabPane.getTabs().add(depotWertpapierTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotStruktur.fxml", depotStrukturController);
        depotStrukturTab = createStyledTab("Struktur", parent);
        portfolioManagementTabPane.getTabs().add(depotStrukturTab);

        depotPlanungController = new DepotPlanungController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotPlanung.fxml", depotPlanungController);
        depotPlanungTab = createStyledTab("Planung", parent);
        portfolioManagementTabPane.getTabs().add(depotPlanungTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/planung/depotPlanungWertpapierVergleich.fxml", depotPlanungWertpapiervergleichController);
        depotPlanungVergleichTab = createStyledTab("Wertpapiervergleich", parent);
        portfolioManagementTabPane.getTabs().add(depotPlanungVergleichTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/planung/depotPlanungOrders.fxml", depotPlanungOrderController);
        depotPlanungOrdersTab = createStyledTab("Orders", parent);
        portfolioManagementTabPane.getTabs().add(depotPlanungOrdersTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotTransaktionen.fxml", depotTransaktionenController);
        depotTransaktionenTab = createStyledTab("Transaktionen", parent);
        portfolioManagementTabPane.getTabs().add(depotTransaktionenTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotAnlagestrategie.fxml", depotAnlagestrategieController);
        depotAnlageStrategieTab = createStyledTab("Anlagestrategie", parent);
        portfolioManagementTabPane.getTabs().add(depotAnlageStrategieTab);


        kontoListController = new KontoListController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/kontos.fxml", kontoListController);
        kontoTab = createStyledTab("Konten", parent);
        portfolioManagementTabPane.getTabs().add(kontoTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/konto/kontoOverview.fxml", kontoOverviewController);
        kontoÜbersichtTab = createStyledTab("Übersicht", parent);
        portfolioManagementTabPane.getTabs().add(kontoÜbersichtTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/konto/kontoTransactions.fxml", kontoTransactionsController);
        kontoTransaktionenTab = createStyledTab("Transaktionen", parent);
        portfolioManagementTabPane.getTabs().add(kontoTransaktionenTab);


        ownerListController = new OwnerListController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owners.fxml", ownerListController);
        inhaberTab = createStyledTab("Inhaber", parent);
        portfolioManagementTabPane.getTabs().add(inhaberTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerOverview.fxml", ownerOverviewController);
        inhaberÜbersichtTab = createStyledTab("Inhaber", parent);
        portfolioManagementTabPane.getTabs().add(inhaberÜbersichtTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerVermögen.fxml", ownerVermögenController);
        inhaberVermögenTab = createStyledTab("Inhaber", parent);
        portfolioManagementTabPane.getTabs().add(inhaberVermögenTab);


        portfolioManagementTabPane.setStyle("-fx-tab-min-height: 30px;" + "-fx-tab-max-height: 30px;" + "-fx-tab-min-width: 150px;" + "-fx-tab-max-width: 150px;" + "-fx-alignment: CENTER;");

        showPortfolioManagementTabs();
    }

    // Hilfsmethode zur Erstellung von Tabs mit angepasstem Stil
    private Tab createStyledTab(String title, Parent parent) {
        Tab tab = new Tab(title, parent);
        tab.setStyle("-fx-background-color: #FFF;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                tab.setStyle("-fx-background-color: #DCDCDC;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
            } else {
                tab.setStyle("-fx-background-color: #FFF;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
            }
        });


        return tab;
    }

    private void hideAllTabs() {
        portfolioManagementTabPane.getTabs().clear();
    }

    private void hideTab(Tab tab) {
        portfolioManagementTabPane.getTabs().remove(tab);
    }

    private void addTab(Tab tab) {
        portfolioManagementTabPane.getTabs().add(tab);
    }

    public void showPortfolioManagementTabs() {
        hideAllTabs();
        addTab(portfoliosTab);
        addTab(depotTab);
        addTab(kontoTab);
        addTab(inhaberTab);
        portfolioManagementTabPane.getSelectionModel().select(portfoliosTab);
    }

    public void showPortfolioTabs() {
        hideAllTabs();
        addTab(depotTab);
        addTab(portfolioStrukturTab);
        addTab(kontoTab);
        addTab(portfolioAnalyseTab);
        addTab(portfolioBenchmarkTab);
        portfolioManagementTabPane.getSelectionModel().select(depotTab);
    }

    public void showDepotTabs() {
        hideAllTabs();
        addTab(depotWertpapierTab);
        addTab(depotStrukturTab);
        addTab(depotPlanungTab);
        addTab(depotTransaktionenTab);
        addTab(depotAnlageStrategieTab);
        portfolioManagementTabPane.getSelectionModel().select(depotWertpapierTab);
    }

    public void showDepotPlanungTabs() {
        hideAllTabs();
        addTab(depotPlanungVergleichTab);
        addTab(depotPlanungOrdersTab);
        portfolioManagementTabPane.getSelectionModel().select(depotPlanungVergleichTab);
    }

    public void showKontoTabs() {
        hideAllTabs();
        addTab(kontoÜbersichtTab);
        addTab(kontoTransaktionenTab);
        portfolioManagementTabPane.getSelectionModel().select(kontoÜbersichtTab);
    }

    public void showInhaberTabs() {
        hideAllTabs();
        addTab(inhaberÜbersichtTab);
        addTab(inhaberVermögenTab);
        addTab(portfoliosTab);
        addTab(depotTab);
        addTab(kontoTab);
        portfolioManagementTabPane.getSelectionModel().select(inhaberÜbersichtTab);
    }
}
