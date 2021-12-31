package de.tud.inf.mmt.wmscrape.gui.tabs;

import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller.DataTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller.ScrapingTabController;
import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class PrimaryTabController {

    @FXML private Button logoutButton;
    @FXML private TabPane primaryTabPane;
    @FXML private Label currentUserLabel;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    private ImportTabController importTabController;
    @Autowired
    private ScrapingTabController scrapingTabController;
    @Autowired
    private DataTabController dataTabController;

    /**
     * called when loading the fxml file
     */
    @FXML
    private void initialize() throws IOException {
        currentUserLabel.setText("Aktueller Nutzer: " + SpringIndependentData.getUsername());

        Parent parent = PrimaryTabManager.loadTabFxml("gui/tabs/dbdata/controller/dataTab.fxml", dataTabController);
        Tab dataTab = new Tab("Daten" , parent);
        primaryTabPane.getTabs().add(dataTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/imports/controller/importTab.fxml", importTabController);
        Tab importTab = new Tab("Import" , parent);
        primaryTabPane.getTabs().add(importTab);

        parent = PrimaryTabManager.loadTabFxml("gui/tabs/scraping/controller/scrapingTab.fxml", scrapingTabController);
        Tab tab = new Tab("Scraping" , parent);
        primaryTabPane.getTabs().add(tab);

        primaryTabPane.getSelectionModel().selectedItemProperty().addListener((o,ov,nv) -> {
            // can't know when the scraping service finished so refresh on select
            if (nv.equals(dataTab)) dataTabController.handleResetButton();
            if (nv.equals(importTab)) importTabController.refreshCorrelationTables();
        });

    }

    /**
     * closes the spring application context and returns to the login menu
     */
    @FXML
    private void handleLogoutButton() {
        applicationContext.close();
        PrimaryTabManager.loadFxml("gui/login/controller/existingUserLogin.fxml", "Login", logoutButton, false, null);
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
