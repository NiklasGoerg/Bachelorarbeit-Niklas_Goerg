package de.tud.inf.mmt.wmscrape.gui.login.manager;

import de.tud.inf.mmt.wmscrape.WMScrape;
import de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class LoginManager {

    /**
     * loads the properties from the user.properties file and stores them in
     * {@link de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData}
     */
    public static void loadUserProperties() {
        Properties prop = new Properties();
        String lastUsername = "";
        String lastDbPath = "mysql://localhost/";

        try(FileInputStream f = new FileInputStream("src/main/resources/user.properties")) {
            prop.load(f);
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
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            properties.setProperty("last.username",username);
            properties.store( new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * sets the parameters for the spring DataSource-Bean and creates a new task in which spring does its initialization
     *
     * @param username unmodified username from the text-field
     * @param password the password for the mysql-useraccount
     * @param progress the progress bar either from {@link de.tud.inf.mmt.wmscrape.gui.login.controller.ExistingUserLoginController}
     *                 or {@link de.tud.inf.mmt.wmscrape.gui.login.controller.NewUserLoginController}
     * @param button the button which is hidden when displaying the progress
     * @return true if successful login without errors
     */
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
        // task runs in the background at this moment
        return true;
    }


    /**
     * injects the spring bean context to the main controller class and loads the first window behind the login
     *
     * @param control some fxml element used for accessing the scene/stage
     * @param context the spring application context created by the background task in
     * {@link de.tud.inf.mmt.wmscrape.gui.login.manager.LoginManager#loginAsUser(String, String, ProgressIndicator, Button)}
     */
    public static void injectContext(Control control, ConfigurableApplicationContext context) {
        // is static to be able to use it inside the anonymous function / lambda
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


    /**
     * uses jdbc to check if every requirement to create a user is met and creates the new user if so.
     * the username ist modified to represent a valid mysql username
     *
     * @param rootUn the mysql-account username with rights to create users
     * @param rootPw the mysql-account password with rights to create users
     * @param newUn the unmodified username for the new user
     * @param newPw the mysql-account password for the new user
     * @return int value used to transport error messages to the caller {@link de.tud.inf.mmt.wmscrape.gui.login.controller.NewUserLoginController}
     */
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
            if (userDbExists(connection, newUnWithoutSpaces)) {
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

    /**
     * checks if the "root" user has the right to create a user by checking the access to the sys database
     *
     * @param connection jdbc connection
     * @return true if sys database can be accessed
     */
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

    /**
     * checks if a user already exists
     *
     * @param connection jdbc connection
     * @param newUsername the modified username
     * @return true if user already exists
     */
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

    /**
     * checks if a database for the user already exists
     *
     * @param connection jdbc connection
     * @param newUsername the modified username
     * @return true if the db exists
     */
    private static boolean userDbExists(Connection connection, String newUsername) {
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

    /**
     * creates the user by some special sql-statements which allow escaping the passwords correctly by using
     * prepared statements and sql quote function
     *
     * @param connection jdbc connection
     * @param newUsername the modified username
     * @param newPassword the new user password
     * @return true if successful
     */
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

    /**
     * checks if a connection to the database can be made given the parameters
     *
     * @param path path to the db
     * @param username modified username
     * @param password unmodified password
     * @return true if connection can be made
     */
    private static boolean connectionValid(String path, String username, String password) {

        try {
            Connection connection = DriverManager.getConnection(path, username, password);
            return !connection.isClosed();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     *
     * @param path path to the db
     * @param username modified username
     * @param password unmodified password
     * @return a jdbc connection
     */
    private static Connection getConnection(String path, String username, String password) {
        try {
            return DriverManager.getConnection(path, username, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * creating the connection path by using the username
     *
     * @param username modified username
     * @param propertyPath the connection path which is stored in {@link de.tud.inf.mmt.wmscrape.springdata.SpringIndependentData}
     * @return the connection path string
     */
    private static String formSpringConnectionPath(String username, String propertyPath) {
        String removedTrailingSlash = propertyPath.replaceAll("/$", "");
        return "jdbc:"+removedTrailingSlash+"/"+username+"_wms_db";
    }


    /**
     * used for various kinds of errors
     * @param e the exception
     * @param control a fxml element used for reference
     */
    public static void programErrorAlert(Throwable e, Control control) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Fehler bei dem Starten des Programms!\n"+e.getCause(), ButtonType.CLOSE);
        alert.setHeaderText("Programmfehler");
        var window = control.getScene().getWindow();
        alert.setX(window.getX()+(window.getWidth()/2)-200);
        alert.setY(window.getY()+(window.getHeight()/2)-200);
        alert.showAndWait();
    }

    /**
     * shows the login button again if an error occurs
     *
     * @param bar the progress indicator
     * @param button the login button
     */
    private static void showLoginButtonAgain(ProgressIndicator bar, Button button) {
        bar.setVisible(false);
        bar.setManaged(false);

        button.setVisible(true);
        button.setManaged(true);
    }
 }

