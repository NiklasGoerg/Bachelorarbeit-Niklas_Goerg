package de.tud.inf.mmt.wmscrape.gui;

import de.tud.inf.mmt.wmscrape.springdata.SpringContextAccessor;
import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GuiApplication extends Application {

    @Override
        public void init() {}

        @Override
        public void start(Stage stage) {

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource("gui/login/controller/existingUserLogin.fxml"));
                Parent parent = fxmlLoader.load();
                stage.setScene(new Scene(parent));
                stage.getScene().getStylesheets().add("style.css");
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void stop() {
            PrimaryTabController controller = SpringContextAccessor.getBean(PrimaryTabController.class);
            if (controller != null) {
                ConfigurableApplicationContext context = controller.getApplicationContext();
                if (context != null && context.isRunning()) {
                    context.close();
                }
            }
            Platform.exit();
        }
    }
