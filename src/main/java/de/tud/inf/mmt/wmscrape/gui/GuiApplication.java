package de.tud.inf.mmt.wmscrape.gui;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabController;
import de.tud.inf.mmt.wmscrape.springdata.SpringContextAccessor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class GuiApplication extends Application {

        @Override
        public void init() {}

    /**
     * the entry point for the javafx application
     *
     * @param stage the initial stage is created by javafx and can be used as a starting point
     */
    @Override
        public void start(Stage stage) {

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource("gui/login/controller/existingUserLogin.fxml"));
                Parent parent = fxmlLoader.load();
                stage.setScene(new Scene(parent));
                stage.getScene().getStylesheets().add("style.css");
                stage.getIcons().add(new Image(Objects.requireNonNull(WMScrape.class.getResourceAsStream("/icon.png"))));
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    /**
     * called when closing the program window.
     * tries to stop the spring context if one was created
     */
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
