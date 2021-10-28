package de.tud.inf.mmt.wmscrape.gui.tabs;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.appdata.SpringIndependentData;
import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller.ImportTabController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.FloatBuffer;

@Component
public class PrimaryTabController {

    @FXML private Button logoutButton;
    @FXML private TabPane primaryTabPane;
    @FXML private Label currentUserLabel;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    ImportTabController importTabController;

    @FXML
    private void initialize() throws IOException {
        currentUserLabel.setText("Aktueller Nutzer: " + SpringIndependentData.getUsername());

        Parent parent = loadTabFxml("gui/tabs/imports/controller/importTab.fxml", importTabController);
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

    private Parent loadTabFxml(String path, Object controllerClass) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(path));
        fxmlLoader.setControllerFactory(param -> controllerClass);
        return fxmlLoader.load();
    }
}
