package de.tud.inf.mmt.wmscrape.gui.tabs;

import de.tud.inf.mmt.wmscrape.WMScrape;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PrimaryTabManagement {

    public Parent loadTabFxml(String path, Object controllerClass) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(path));
        // this is the key to make spring available to the controller behind the fxml
        fxmlLoader.setControllerFactory(param -> controllerClass);
        return fxmlLoader.load();
    }

    public void loadFxml(String path, String stageTitle, Control control, boolean isModal, Object controllerClass) {
        Parent parent;

        try {
            parent = loadTabFxml(path, controllerClass);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage stage;

        if(isModal) {
            stage = new Stage();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.getScene().getStylesheets().add("style.css");
            stage.initOwner(control.getScene().getWindow());
            stage.show();
        } else {
            stage = (Stage) control.getScene().getWindow();
            stage.getScene().getStylesheets().add("style.css");
            stage.getScene().setRoot(parent);

        }
        stage.setTitle(stageTitle);
    }
}
