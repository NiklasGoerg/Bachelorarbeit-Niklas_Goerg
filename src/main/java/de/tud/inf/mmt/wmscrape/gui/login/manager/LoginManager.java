package de.tud.inf.mmt.wmscrape.gui.login.manager;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.appdata.SpringIndependentData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class LoginManager {

    private static ConfigurableApplicationContext applicationContext;


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

    public static void styleOnValid(ValidationSupport validation, Control control, String invalidStyle) {
        if(validation.isInvalid()) {
            control.getStyleClass().add(invalidStyle);
        } else {
            control.getStyleClass().remove(invalidStyle);
        }
    }

    public static boolean loginExistingUser(String username, String password, Control control) {
        String springUsername = username.trim().replace(" ", "_");
        String springConnectionPath = formSpringConnectionPath(springUsername, SpringIndependentData.getPropertyConnectionPath());

        // tries to establish a connection
        if (!connectionValid(springConnectionPath, springUsername, password)) {
            return false;
        }

        // if successful save username for next time and set the value to be fetched by DataSourceConfig
        saveUsernameProperty(username);
        SpringIndependentData.setSpringConnectionPath(springConnectionPath);
        SpringIndependentData.setUsername(springUsername);
        SpringIndependentData.setPassword(password);

        // spring starts here
        applicationContext = new SpringApplicationBuilder(WMScrape.class).run();
        FXMLLoader fxmlLoader = new FXMLLoader(WMScrape.class.getResource("gui/tabs/primaryTab.fxml"));
        // spring context is injected
        fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
        Parent parent;

        try {
            parent = fxmlLoader.load();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }

        Stage window = (Stage) control.getScene().getWindow();
        window.getScene().setRoot(parent);
        window.setTitle("WMScrape");

        return true;
    }

    public static int createUser(String rootUn, String rootPw, String newUn, String newPw) {
        String rootConnectionPath = "jdbc:" + SpringIndependentData.getPropertyConnectionPath();
        Connection connection = getConnection(rootConnectionPath, rootUn.trim(), rootPw);
        String newUnWithoutSpaces = newUn.trim().replace(" ", "_");

        if(connection == null) {
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
        } if(!createUserAndDb(connection, newUnWithoutSpaces, newPw)) {
            // unknown error at creation of table and user
            return -5;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return 1;
    }

    private static boolean isRootUser(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("show databases");

            while (results.next()) {
                // only root user has the sys table (no safety against malicious user)
                if (results.getString(1).contentEquals("sys")) {
                    statement.close();
                    return true;
                }
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return false;
    }

    private static boolean userExists(Connection connection, String newUsername) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("use mysql");
            ResultSet results = statement.executeQuery("select user from user");

            while (results.next()) {
                if (results.getString(1).contentEquals(newUsername)) {
                    return true;
                }
            }

            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return true;
        }

        return false;
    }

    private static boolean userTableExists(Connection connection, String newUsername) {
        try {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("show databases");

            while (results.next()) {
                if (results.getString(1).contentEquals(newUsername + "_USER_DB")) {
                    return true;
                }
            }
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return true;
        }

        return false;
    }

    private static boolean createUserAndDb(Connection connection,  String newUsername, String newPassword) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + newUsername + "_USER_DB;");
            statement.executeUpdate(
                    "GRANT ALL PRIVILEGES ON " + newUsername + "_USER_DB.* TO '" +
                    newUsername + "'@'%' IDENTIFIED BY '" +  newPassword + "';");
            statement.executeUpdate("FLUSH PRIVILEGES;");
            statement.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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

    public static String formSpringConnectionPath(String username, String propertyPath) {
        String removedTrailingSlash = propertyPath.replaceAll("/$", "");
        return "jdbc:" + removedTrailingSlash + "/" + username.replace(" ", "_") + "_USER_DB";
    }
 }

