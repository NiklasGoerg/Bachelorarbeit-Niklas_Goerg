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
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.owners.owner.*;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.PortfolioListController;
import de.tud.inf.mmt.wmscrape.gui.tabs.portfoliomanagement.portfolios.portfolio.*;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Controller
public class PortfolioManagementTabController {

    public PortfolioManagementTabController(PortfolioManagementTabManager portfolioManagementTabManager) {
        this.portfolioManagementTabManager = portfolioManagementTabManager;
    }

    public ToolBar breadcrumbContainer;

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
    private PortfolioDepotsController portfolioDepotsController;
    @Autowired
    private PortfolioKontosController portfolioKontosController;

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
    @Autowired
    private OwnerDepotsController ownerDepotsController;
    @Autowired
    private OwnerKontosController ownerKontosController;
    @Autowired
    private OwnerPortfoliosController ownerPortfoliosController;


    private Tab portfoliosTab;
    private Tab portfolioAnalyseTab;
    private Tab portfolioBenchmarkTab;
    private Tab portfolioStrukturTab;
    private Tab portfolioDepotsTab;
    private Tab portfolioKontosTab;

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
    private Tab inhaberDepotsTab;
    private Tab inhaberPortfoliosTab;
    private Tab inhaberKontosTab;

    private Set<Tab> depotTabs;
    private Set<Tab> portfolioTabs;
    private Set<Tab> kontoTabs;
    private Set<Tab> ownerTabs;

    public class ContexMenuItem {
        private String label;
        private Runnable action;

        public ContexMenuItem(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        public String getLabel() {
            return label;
        }

        public Runnable getAction() {
            return action;
        }

        public void performAction() {
            if (action != null) {
                action.run();
            }
        }
    }

    public ContexMenuItem emptyContexMenuItem = new ContexMenuItem("", null);
    public List<ContexMenuItem> emptyContextMenuItemList = new ArrayList<>();

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() throws IOException {
        portfolioManagementTabManager.setPortfolioController(this);

        portfolioListController = new PortfolioListController(portfolioManagementTabManager);
        Parent parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolios.fxml", portfolioListController);
        portfoliosTab = createStyledTab("Portfolios", parent);
        portfolioManagementTabPane.getTabs().add(portfoliosTab);

        portfolioAnalyseController = new PortfolioAnalyseController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioAnalyse.fxml", portfolioAnalyseController);
        portfolioAnalyseTab = createStyledTab("Analyse", parent);
        portfolioManagementTabPane.getTabs().add(portfolioAnalyseTab);

        portfolioBenchmarkController = new PortfolioBenchmarkController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioBenchmarks.fxml", portfolioBenchmarkController);
        portfolioBenchmarkTab = createStyledTab("Benchmarks", parent);
        portfolioManagementTabPane.getTabs().add(portfolioBenchmarkTab);

        portfolioStrukturController = new PortfolioStrukturController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioStruktur.fxml", portfolioStrukturController);
        portfolioStrukturTab = createStyledTab("Struktur", parent);
        portfolioManagementTabPane.getTabs().add(portfolioStrukturTab);

        portfolioDepotsController = new PortfolioDepotsController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioDepots.fxml", portfolioDepotsController);
        portfolioDepotsTab = createStyledTab("Depots", parent);
        portfolioManagementTabPane.getTabs().add(portfolioDepotsTab);

        portfolioKontosController = new PortfolioKontosController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/portfolios/portfolio/portfolioKontos.fxml", portfolioKontosController);
        portfolioKontosTab = createStyledTab("Kontos", parent);
        portfolioManagementTabPane.getTabs().add(portfolioKontosTab);


        depotListController = new DepotListController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depots.fxml", depotListController);
        depotTab = createStyledTab("Depots", parent);
        portfolioManagementTabPane.getTabs().add(depotTab);

        depotWertpapierController = new DepotWertpapierController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotWertpapier.fxml", depotWertpapierController);
        depotWertpapierTab = createStyledTab("Wertpapiere", parent);
        portfolioManagementTabPane.getTabs().add(depotWertpapierTab);

        depotStrukturController = new DepotStrukturController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotStruktur.fxml", depotStrukturController);
        depotStrukturTab = createStyledTab("Struktur", parent);
        portfolioManagementTabPane.getTabs().add(depotStrukturTab);

        depotPlanungController = new DepotPlanungController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotPlanung.fxml", depotPlanungController);
        depotPlanungTab = createStyledTab("Planung", parent);
        portfolioManagementTabPane.getTabs().add(depotPlanungTab);

        depotPlanungWertpapiervergleichController = new DepotPlanungWertpapiervergleichController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/planung/depotPlanungWertpapierVergleich.fxml", depotPlanungWertpapiervergleichController);
        depotPlanungVergleichTab = createStyledTab("Wertpapiervergleich", parent);
        portfolioManagementTabPane.getTabs().add(depotPlanungVergleichTab);

        depotPlanungOrderController = new DepotPlanungOrderController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/planung/depotPlanungOrders.fxml", depotPlanungOrderController);
        depotPlanungOrdersTab = createStyledTab("Orders", parent);
        portfolioManagementTabPane.getTabs().add(depotPlanungOrdersTab);

        depotTransaktionenController = new DepotTransaktionenController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotTransaktionen.fxml", depotTransaktionenController);
        depotTransaktionenTab = createStyledTab("Transaktionen", parent);
        portfolioManagementTabPane.getTabs().add(depotTransaktionenTab);

        depotAnlagestrategieController = new DepotAnlagestrategieController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/depots/depot/depotAnlagestrategie.fxml", depotAnlagestrategieController);
        depotAnlageStrategieTab = createStyledTab("Anlagestrategie", parent);
        portfolioManagementTabPane.getTabs().add(depotAnlageStrategieTab);


        kontoListController = new KontoListController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/kontos.fxml", kontoListController);
        kontoTab = createStyledTab("Konten", parent);
        portfolioManagementTabPane.getTabs().add(kontoTab);

        kontoOverviewController = new KontoOverviewController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/konto/kontoOverview.fxml", kontoOverviewController);
        kontoÜbersichtTab = createStyledTab("Übersicht", parent);
        portfolioManagementTabPane.getTabs().add(kontoÜbersichtTab);

        kontoTransactionsController = new KontoTransactionsController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/kontos/konto/kontoTransactions.fxml", kontoTransactionsController);
        kontoTransaktionenTab = createStyledTab("Transaktionen", parent);
        portfolioManagementTabPane.getTabs().add(kontoTransaktionenTab);


        ownerListController = new OwnerListController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owners.fxml", ownerListController);
        inhaberTab = createStyledTab("Inhaber", parent);
        portfolioManagementTabPane.getTabs().add(inhaberTab);

        ownerOverviewController = new OwnerOverviewController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerOverview.fxml", ownerOverviewController);
        inhaberÜbersichtTab = createStyledTab("Übersicht", parent);
        portfolioManagementTabPane.getTabs().add(inhaberÜbersichtTab);

        ownerVermögenController = new OwnerVermögenController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerVermögen.fxml", ownerVermögenController);
        inhaberVermögenTab = createStyledTab("Vermögen", parent);
        portfolioManagementTabPane.getTabs().add(inhaberVermögenTab);

        ownerPortfoliosController = new OwnerPortfoliosController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerPortfolios.fxml", ownerPortfoliosController);
        inhaberPortfoliosTab = createStyledTab("Portfolios", parent);
        portfolioManagementTabPane.getTabs().add(inhaberPortfoliosTab);

        ownerKontosController = new OwnerKontosController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerKontos.fxml", ownerKontosController);
        inhaberKontosTab = createStyledTab("Kontos", parent);
        portfolioManagementTabPane.getTabs().add(inhaberKontosTab);

        ownerDepotsController = new OwnerDepotsController(portfolioManagementTabManager);
        parent = PrimaryTabManager.loadTabFxml("gui/tabs/portfoliomanagement/owners/owner/ownerDepots.fxml", ownerDepotsController);
        inhaberDepotsTab = createStyledTab("Depots", parent);
        portfolioManagementTabPane.getTabs().add(inhaberDepotsTab);


        portfolioManagementTabPane.setStyle("-fx-tab-min-height: 30px;" + "-fx-tab-max-height: 30px;" + "-fx-tab-min-width: 150px;" + "-fx-tab-max-width: 150px;" + "-fx-alignment: CENTER;");


        depotTabs = Set.of(depotWertpapierTab, depotStrukturTab, depotTransaktionenTab, depotAnlageStrategieTab, depotPlanungTab);
        portfolioTabs = Set.of(portfolioStrukturTab, portfolioAnalyseTab, portfolioBenchmarkTab);
        kontoTabs = Set.of(kontoÜbersichtTab, kontoTransaktionenTab);
        ownerTabs = Set.of(inhaberVermögenTab, inhaberÜbersichtTab);
        showPortfolioManagementTabs();


        portfolioManagementTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == depotPlanungTab) {
                showDepotPlanungTabs();
                changeBreadcrumbs(portfolioManagementTabManager.getCurrentlyDisplayedElements());
                addDepotPlanungBreadcrumbs();
            }

        });
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

    private void hideTab(Tab tab) {
        portfolioManagementTabPane.getTabs().remove(tab);
    }

    private void hideAllTabs() {
        portfolioManagementTabPane.getTabs().clear();
    }

    private void addTab(Tab tab) {
        portfolioManagementTabPane.getTabs().add(tab);
    }

    public void showDepotTabs() {
        removeBreadcrumbs();
        hideAllTabs();
        addTab(depotWertpapierTab);
        addTab(depotStrukturTab);
        addTab(depotPlanungTab);
        addTab(depotTransaktionenTab);
        addTab(depotAnlageStrategieTab);
        portfolioManagementTabPane.getSelectionModel().select(depotWertpapierTab);
    }

    public void showPortfolioManagementTabs() {
        removeBreadcrumbs();
        hideAllTabs();
        addTab(portfoliosTab);
        addTab(depotTab);
        addTab(kontoTab);
        addTab(inhaberTab);
        portfolioManagementTabPane.getSelectionModel().select(portfoliosTab);
    }

    public void showPortfolioTabs() {
        removeBreadcrumbs();
        hideAllTabs();
        addTab(portfolioDepotsTab);
        addTab(portfolioStrukturTab);
        addTab(portfolioKontosTab);
        addTab(portfolioAnalyseTab);
        addTab(portfolioBenchmarkTab);
        portfolioManagementTabPane.getSelectionModel().select(depotTab);
    }


    public void showDepotPlanungTabs() {
        removeBreadcrumbs();
        hideAllTabs();
        addTab(depotPlanungVergleichTab);
        addTab(depotPlanungOrdersTab);
        portfolioManagementTabPane.getSelectionModel().select(depotPlanungVergleichTab);
    }

    public void showKontoTabs() {
        removeBreadcrumbs();
        hideAllTabs();
        addTab(kontoÜbersichtTab);
        addTab(kontoTransaktionenTab);
        portfolioManagementTabPane.getSelectionModel().select(kontoÜbersichtTab);
    }

    public void showInhaberTabs() {
        removeBreadcrumbs();
        hideAllTabs();
        addTab(inhaberÜbersichtTab);
        addTab(inhaberVermögenTab);
        addTab(inhaberPortfoliosTab);
        addTab(inhaberDepotsTab);
        addTab(inhaberKontosTab);
        portfolioManagementTabPane.getSelectionModel().select(inhaberÜbersichtTab);
    }

    public void createBreadcrumbInstance(String label, List<ContexMenuItem> captions, Runnable onLabelClick, String type) {
        Label label1 = new Label(label);

        ContextMenu contextMenu = new ContextMenu();

        for (ContexMenuItem caption : captions) {
            MenuItem menuItem = new MenuItem(caption.label);
            contextMenu.getItems().add(menuItem);
            contextMenu.setOnAction(event -> {
                removeBreadcrumbs();
                caption.performAction();
            });
        }

        // setContextMenu to label
        label1.setContextMenu(contextMenu);
        label1.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                onLabelClick.run();
            }
        });
        breadcrumbContainer.getItems().add(label1);
    }

    public void removeBreadcrumbs() {
        breadcrumbContainer.getItems().removeIf(item -> item instanceof Label);
    }

    public void changeBreadcrumbs(List<BreadcrumbElement> elements) {
        removeBreadcrumbs();
        for (BreadcrumbElement element : elements) {
            switch (element.type) {
                case "depot" -> addDepotBreadcrumbs(element.element, portfolioManagementTabManager.depotList);
                case "portfolio" ->
                        addPortfolioBreadcrumbs(element.element, portfolioManagementTabManager.portfolioList);
                case "owner" -> addOwnerBreadcrumbs(element.element, portfolioManagementTabManager.ownerList);
                case "konto" -> addKontoBreadcrumbs(element.element, portfolioManagementTabManager.kontoList);
            }
        }
    }

    //checks if breadcrumbs contain certain type
    public boolean breadcrumbsContainType(String type) {
        boolean containsType = false;
        for (BreadcrumbElement element : portfolioManagementTabManager.getCurrentlyDisplayedElements()) {
            if (element.type.equals(type)) {
                containsType = true;
                break; // Sobald ein passendes Objekt gefunden wurde, die Schleife beenden
            }
        }
        return containsType;
    }

    public void addDepotBreadcrumbs(String chosenDepot, String[] otherDepots) {
        List<ContexMenuItem> contextMenuList = new ArrayList<>();

        //if user is in portfolio view
        if (breadcrumbsContainType("portfolio")) {
            //set up context menu items
            for (String depotName : portfolioManagementTabManager.depotsOfPortfolio1List) {
                ContexMenuItem newItem = new ContexMenuItem(depotName, () -> {
                    portfolioManagementTabManager.removeLastCurrentlyDisplayedElement();
                    portfolioManagementTabManager.addCurrentlyDisplayedElement(new BreadcrumbElement(depotName, "depot"));
                });
                contextMenuList.add(newItem);
            }
            //create Breadcrumb for "/ Depots"
            createBreadcrumbInstance("/ Depots / ", emptyContextMenuItemList, () -> {
                showPortfolioTabs();
                portfolioManagementTabManager.removeLastCurrentlyDisplayedElement();
                changeBreadcrumbs(portfolioManagementTabManager.getCurrentlyDisplayedElements());
            }, "depot");
            //create Breadcrumb for specific depot
            createBreadcrumbInstance(chosenDepot, contextMenuList, () -> {
                showDepotTabs();
                changeBreadcrumbs(portfolioManagementTabManager.getCurrentlyDisplayedElements());
            }, "depot");
        } else {
            for (String depotName : otherDepots) {
                ContexMenuItem newItem = new ContexMenuItem(depotName, () -> portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement(depotName, "depot")));
                contextMenuList.add(newItem);
            }
            createBreadcrumbInstance("Depots / ", emptyContextMenuItemList, this::showPortfolioManagementTabs, "depot");
            createBreadcrumbInstance(chosenDepot, contextMenuList, () -> {
                showDepotTabs();
                addDepotBreadcrumbs(chosenDepot, otherDepots);
            }, "depot");
        }


    }

    public void addPortfolioBreadcrumbs(String chosenPortfolio, String[] otherPortfolios) {
        List<ContexMenuItem> contextMenuList = new ArrayList<>();
        for (String portfolioName : otherPortfolios) {
            ContexMenuItem newItem = new ContexMenuItem(portfolioName, () -> portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement(portfolioName, "portfolio")));
            contextMenuList.add(newItem);
        }
        createBreadcrumbInstance("Portfolios / ", emptyContextMenuItemList, this::showPortfolioManagementTabs, "portfolio");
        createBreadcrumbInstance(chosenPortfolio, contextMenuList, () -> {
            showPortfolioTabs();
            addPortfolioBreadcrumbs(chosenPortfolio, otherPortfolios);
        }, "portfolio");
    }

    public void addOwnerBreadcrumbs(String chosenOwner, String[] otherOwners) {
        List<ContexMenuItem> contextMenuList = new ArrayList<>();
        for (String ownerName : otherOwners) {
            ContexMenuItem newItem = new ContexMenuItem(ownerName, () -> portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement(ownerName, "portfolio")));
            contextMenuList.add(newItem);
        }
        createBreadcrumbInstance("Inhaber / ", emptyContextMenuItemList, this::showPortfolioManagementTabs, "owner");
        createBreadcrumbInstance(chosenOwner, contextMenuList, () -> {
            showInhaberTabs();
            addOwnerBreadcrumbs(chosenOwner, otherOwners);
        }, "owner");
    }

    public void addKontoBreadcrumbs(String chosenKonto, String[] otherKontos) {
        List<ContexMenuItem> contextMenuList = new ArrayList<>();

        if (breadcrumbsContainType("portfolio")) {
            for (String kontoName : portfolioManagementTabManager.kontosOfPortfolio1List) {
                ContexMenuItem newItem = new ContexMenuItem(kontoName, () -> {
                    portfolioManagementTabManager.removeLastCurrentlyDisplayedElement();
                    portfolioManagementTabManager.addCurrentlyDisplayedElement(new BreadcrumbElement(kontoName, "konto"));
                });
                contextMenuList.add(newItem);
            }
            createBreadcrumbInstance("/ Konten / ", emptyContextMenuItemList, () -> {
                showPortfolioTabs();
                portfolioManagementTabManager.removeLastCurrentlyDisplayedElement();
                changeBreadcrumbs(portfolioManagementTabManager.getCurrentlyDisplayedElements());
            }, "konto");
            createBreadcrumbInstance(chosenKonto, contextMenuList, () -> {
                showPortfolioTabs();
                changeBreadcrumbs(portfolioManagementTabManager.getCurrentlyDisplayedElements());
            }, "depot");
        } else {
            for (String kontoName : otherKontos) {
                ContexMenuItem newItem = new ContexMenuItem(kontoName, () -> portfolioManagementTabManager.setCurrentlyDisplayedElement(new BreadcrumbElement(kontoName, "portfolio")));
                contextMenuList.add(newItem);
            }
            createBreadcrumbInstance("Konten / ", emptyContextMenuItemList, this::showPortfolioManagementTabs, "konto");
            createBreadcrumbInstance(chosenKonto, contextMenuList, () -> {
                showKontoTabs();
                addKontoBreadcrumbs(chosenKonto, otherKontos);
            }, "konto");
        }

    }

    public void addDepotPlanungBreadcrumbs() {
        createBreadcrumbInstance(" / Planung", emptyContextMenuItemList, () -> {
        }, "");
    }
}
