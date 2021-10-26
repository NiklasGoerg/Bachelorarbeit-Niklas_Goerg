package de.tud.inf.mmt.wmscrape.gui.login.controller;

import de.tud.inf.mmt.wmscrape.WMScrape;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class ExistingUserLoginController {
    @FXML Button button;

    public static ConfigurableApplicationContext applicationContext;

    @FXML
    private void handleButton() throws IOException {
        applicationContext = new SpringApplicationBuilder(WMScrape.class).run();

        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource("gui/tabs/primaryTab.fxml"));
        fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
        Parent parent = fxmlLoader.load();
        Stage window = (Stage) button.getScene().getWindow();
        window.getScene().setRoot(parent);
    }
}
