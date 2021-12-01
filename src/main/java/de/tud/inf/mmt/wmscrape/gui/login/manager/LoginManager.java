package de.tud.inf.mmt.wmscrape.gui.login.manager;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class LoginManager {

    public static void loadFxml(String source, String stageTitle, Control control, boolean isModal) {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource(source));
        Parent parent;

        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            System.out.println(e.getMessage());
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

    public static void closeWindow(Control control) {
        control.getScene().getWindow().hide();
    }

    public static void loadUserProperties() {
        Properties prop = new Properties();
        String lastUsername = "";
        String lastDbPath = "mysql://localhost/";

        try {
            prop.load(new FileInputStream("src/main/resources/user.properties"));
            lastUsername = prop.getProperty("last.username","");
            lastDbPath = prop.getProperty("last.dbPath", "mysql://localhost/");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        SpringIndependentData.setUsername(lastUsername);
        SpringIndependentData.setPropertyConnectionPath(lastDbPath);
    }

    public static void saveUsernameProperty(String username) {
        Properties properties = new Properties();

        try {
            properties.load(WMScrape.class.getClassLoader().getResourceAsStream("user.properties"));
            properties.setProperty("last.username",username);
            properties.store( new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean loginAsUser(String username, String password, ProgressIndicator progress, Button button) {
        String springUsername = username.trim().replace(" ", "_").toLowerCase();
        String springConnectionPath = formSpringConnectionPath(springUsername, SpringIndependentData.getPropertyConnectionPath());

        // tries to establish a connection
        if (!connectionValid(springConnectionPath, springUsername, password)) {
            return false;
        }

        // if successful
        // save username for next time
        saveUsernameProperty(username);
        // set the value to be fetched by DataSourceConfig
        SpringIndependentData.setSpringConnectionPath(springConnectionPath);
        SpringIndependentData.setUsername(springUsername);
        SpringIndependentData.setPassword(password);


        // starts a new task which sole job it is to initialize spring
        // depending on the data to be initialized this can take a moment
        Task<ConfigurableApplicationContext> task = new Task<>() {
            @Override
            protected ConfigurableApplicationContext call() {
                return new SpringApplicationBuilder(WMScrape.class).run();
            }
        };

        // create the task
        Thread th = new Thread(task);
        th.setDaemon(true);

        // use the application context to inject it into the controllers behind the login menu
        task.setOnSucceeded(event -> injectContext(button, task.getValue()));
        // if spring throws an error, create an alert
        task.setOnFailed(evt -> {
            programErrorAlert(task.getException(), button);
            showLoginButtonAgain(progress, button);
        });

        // start the task
        th.start();

        // only here to not show the unsuccessful alert in the controller
        // task tuns in the background at this moment
        return true;
    }


    // is static to be able to use it inside the anonymous function / lambda
    public static void injectContext(Control control, ConfigurableApplicationContext context) {
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource("gui/tabs/primaryTab.fxml"));
        // spring context is injected
        fxmlLoader.setControllerFactory(context::getBean);
        Parent parent;

        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            programErrorAlert(e, control);
            return;
        }

        Stage window = (Stage) control.getScene().getWindow();
        window.getScene().setRoot(parent);
        window.setTitle("WMScrape");
    }


    public static int createUser(String rootUn, String rootPw, String newUn, String newPw) {
        String rootConnectionPath = "jdbc:" + SpringIndependentData.getPropertyConnectionPath();

        try (Connection connection = getConnection(rootConnectionPath, rootUn.trim(), rootPw)) {
            String newUnWithoutSpaces = newUn.trim().replace(" ", "_").toLowerCase();

            if (connection == null) {
                // can't connect with root
                return -1;
            }
            if (!isRootUser(connection)) {
                // connected as non-root
                return -2;
            }
            if (userExists(connection, newUnWithoutSpaces)) {
                // user already exists in the database
                return -3;
            }
            if (userTableExists(connection, newUnWithoutSpaces)) {
                // user table already exist in the database
                return -4;
            }
            if (!createUserAndDb(connection, newUnWithoutSpaces, newPw)) {
                // unknown error at creation of table and user
                return -5;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 1;
    }

    private static boolean isRootUser(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            ResultSet results = statement.executeQuery("show databases");

            while (results.next()) {
                // only root user has the sys table (no safety against malicious user)
                if (results.getString(1).contentEquals("sys")) {
                    statement.close();
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return false;
    }

    private static boolean userExists(Connection connection, String newUsername) {
        try (Statement statement = connection.createStatement()){
            statement.execute("use mysql");
            ResultSet results = statement.executeQuery("select user from user");

            while (results.next()) {
                if (results.getString(1).contentEquals(newUsername)) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return true;
        }

        return false;
    }

    private static boolean userTableExists(Connection connection, String newUsername) {
        try (Statement statement = connection.createStatement()){
            ResultSet results = statement.executeQuery("show databases");

            while (results.next()) {
                if (results.getString(1).contentEquals(newUsername + "_wms_db")) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return true;
        }

        return false;
    }

    private static boolean createUserAndDb(Connection connection,  String newUsername, String newPassword) {
        try {

            String newDbName = newUsername+"_wms_db";
            PreparedStatement pst = connection.prepareStatement("SET @user := ?, @pass := ?, @db := ?;");
            pst.setString(1, newUsername);
            pst.setString(2, newPassword);
            pst.setString(3, newDbName);
            pst.execute();

            pst.execute("SET @sql := CONCAT(\"CREATE USER \", QUOTE(@user), \"@'%' IDENTIFIED BY \", QUOTE(@pass));");
            pst.execute("PREPARE stmt FROM @sql;");
            pst.execute("EXECUTE stmt;");

            pst.execute("SET @sql := CONCAT(\"CREATE DATABASE IF NOT EXISTS  \", @db, \";\");");
            pst.execute("PREPARE stmt FROM @sql;");
            pst.execute("EXECUTE stmt;");

            pst.execute("SET @sql := CONCAT(\"GRANT ALL PRIVILEGES ON \", @db, \".* TO \", QUOTE(@user), \"@'%'\");");
            pst.execute("PREPARE stmt FROM @sql;");
            pst.execute("EXECUTE stmt;");

            pst.execute("FLUSH PRIVILEGES;");
            pst.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean connectionValid(String path, String username, String password) {

        try {
            Connection connection = DriverManager.getConnection(path, username, password);
            return !connection.isClosed();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static Connection getConnection(String path, String username, String password) {
        try {
            return DriverManager.getConnection(path, username, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static String formSpringConnectionPath(String username, String propertyPath) {
        String removedTrailingSlash = propertyPath.replaceAll("/$", "");
        return "jdbc:"+removedTrailingSlash+"/"+username+"_wms_db";
    }


    public static void programErrorAlert(Throwable e, Control control) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Fehler bei dem Starten des Programms!\n"+e.getCause(), ButtonType.CLOSE);
        alert.setHeaderText("Programmfehler");
        var window = control.getScene().getWindow();
        alert.setX(window.getX()+(window.getWidth()/2)-200);
        alert.setY(window.getY()+(window.getHeight()/2)-200);
        alert.showAndWait();

        control.setVisible(false);
        control.setManaged(false);
    }

    private static void showLoginButtonAgain(ProgressIndicator bar, Button button) {
        bar.setVisible(false);
        bar.setManaged(false);

        button.setVisible(true);
        button.setManaged(true);
    }
 }

