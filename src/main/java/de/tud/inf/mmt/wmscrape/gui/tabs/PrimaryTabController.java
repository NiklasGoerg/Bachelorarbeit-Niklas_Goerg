package de.tud.inf.mmt.wmscrape.gui.tabs;

import de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PrimaryTabController {
    @FXML
    private Button logoutButton;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @FXML
    private void initialize() {
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
