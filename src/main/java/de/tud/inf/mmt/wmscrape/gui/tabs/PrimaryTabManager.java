package de.tud.inf.mmt.wmscrape.gui.tabs;

import de.tud.inf.mmt.wmscrape.WMScrape;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PrimaryTabManager {

    /**
     * loads fxml files and injects the spring created controller beans.
     * this is how spring is used in combination with javafx. normally the fxml loader instantiates the
     * connected controller class itself but with setting the controllerFactory the loader uses
     * the controller given as the parameter (here the already instantiated spring bean)
     *
     * @param path the fxml file path
     * @param controllerClass the controller class spring bean
     * @return the javafx parent object which is passed into a stage for visualization
     */
    public static Parent loadTabFxml(String path, Object controllerClass) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(path));
        // this is the key to make spring available to the controller behind the fxml
        fxmlLoader.setControllerFactory(param -> controllerClass);
        return fxmlLoader.load();
    }

    /**
     * also used for loading fxml files but includes modale windows (e.g. popups) and adds the css file
     * (style.css in resources).
     *
     * @param path the fxml file path
     * @param stageTitle the title of the stage (window)
     * @param control some element inside the controller class used as a reference to get the stage/scene
     * @param isModal if true another window is opened
     * @param controllerClass the controller class spring bean
     */
    public static void loadFxml(String path, String stageTitle, Control control, boolean isModal, Object controllerClass, boolean wait) {
        Parent parent;

        try {
            if(controllerClass != null) {
                // load with spring controller
                parent = loadTabFxml(path, controllerClass);
            } else {
                // load with fxml instantiated controller
                FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(path));
                parent = fxmlLoader.load();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        show(isModal, parent, control, stageTitle, wait);
    }

    /**
     *
     * @param isModal if true another window is opened
     * @param parent the parent object loaded from the fxml loader
     * @param control some element inside the controller class used as a reference to get the stage/scene
     * @param stageTitle the title of the stage (window)
     * @param wait wait until the window is closed
     */
    private static void show(boolean isModal, Parent parent, Control control, String stageTitle, boolean wait) {
        Stage stage;

        if(isModal) {
            stage = new Stage();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.getScene().getStylesheets().add("style.css");
            stage.initOwner(control.getScene().getWindow());

            if(wait) {
                stage.showAndWait();
            } else {
                stage.show();
            }
        } else {
            stage = (Stage) control.getScene().getWindow();
            stage.getScene().getStylesheets().add("style.css");
            stage.getScene().setRoot(parent);
        }

        stage.setTitle(stageTitle);
    }

    /**
     * creates a tooltip which can be bound to a javafx field
     *
     * @param text the text message
     * @return the tooltip object
     */
    public static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setOpacity(.9);
        tooltip.setAutoFix(true);
        tooltip.setStyle(".bad-input");
        return tooltip;
    }

    /**
     * tries to center javafx alerts inside the stage from which the alert was created
     *
     * @param alert the alert object
     * @param control some element inside the controller class used as a reference to get the stage/scene
     */
    public static void setAlertPosition(Alert alert, Control control) {
        var window = control.getScene().getWindow();
        alert.setY(window.getY() + (window.getHeight() / 2) - 200);
        alert.setX(window.getX() + (window.getWidth() / 2) - 200);
    }

    /**
     * adds the css styling and a tooltip based on the validation value
     *
     * @param input the javafx element to add styling to
     * @param tooltip the tooltip message to display if invalid
     * @param isValid if false the element will be styled accordingly
     * @param inlineValidationActive used if decoration id only allowed after activating. set to true if no activation needed
     */
    public static void decorateField(Control input, String tooltip, boolean isValid, boolean inlineValidationActive) {
        // see bad-input class in style.css
        input.getStyleClass().remove("bad-input");
        input.setTooltip(null);

        if(!isValid && inlineValidationActive) {
            input.setTooltip(PrimaryTabManager.createTooltip(tooltip));
            input.getStyleClass().add("bad-input");
        }
    }
}
