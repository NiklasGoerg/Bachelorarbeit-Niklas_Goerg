package de.tud.inf.mmt.wmscrape.gui.tabs.scraping;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.historic.controller.HistoricTabController;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class ScrapingTabsController {

    @FXML
    private TabPane scrapingTabPane;

    @Autowired
    private de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingTabController scrapingTabController;
    @Autowired
    private HistoricTabController historicTabController;

    @FXML
    private void initialize() throws IOException {

        Parent parent = PrimaryTabManager.loadTabFxml("gui/tabs/scraping/controller/scrapingTab.fxml", scrapingTabController);
        Tab tab = createStyledTab("Aktuell", parent);
        scrapingTabPane.getTabs().add(tab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/historic/controller/historicTab.fxml", historicTabController);
        Tab historicTab = createStyledTab("Historisch", parent);
        scrapingTabPane.getTabs().add(historicTab);

        //scrapingTabPane.setStyle("-fx-tab-min-height: 30px;" + "-fx-tab-max-height: 30px;" + "-fx-tab-min-width: 150px;" + "-fx-tab-max-width: 150px;" + "-fx-alignment: CENTER;");
    }

    // Hilfsmethode zur Erstellung von Tabs mit angepasstem Stil
    private Tab createStyledTab(String title, Parent parent) {
        Tab tab = new Tab(title, parent);
        //tab.setStyle("-fx-background-color: #FFF;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                //tab.setStyle("-fx-background-color: #DCDCDC;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
            } else {
                //tab.setStyle("-fx-background-color: #FFF;" + "-fx-background-insets: 0, 1;" + "-fx-background-radius: 0, 0 0 0 0;");
            }
        });


        return tab;
    }
}
