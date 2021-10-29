package de.tud.inf.mmt.wmscrape.gui.tabs;

import de.tud.inf.mmt.wmscrape.appdata.SpringIndependentData;
import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
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
    private PrimaryTabManagement primaryTabManagement;

    @FXML
    private void initialize() throws IOException {
        currentUserLabel.setText("Aktueller Nutzer: " + SpringIndependentData.getUsername());

        Parent parent = primaryTabManagement.loadTabFxml("gui/tabs/imports/controller/importTab.fxml", importTabController);
        Tab importTab = new Tab("Import" , parent);
        primaryTabPane.getTabs().add(importTab);
    }

    @FXML
    private void handleLogoutButton() {
        applicationContext.close();
        LoginManager.loadFxml("gui/login/controller/existingUserLogin.fxml", "Login", logoutButton, false);
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
