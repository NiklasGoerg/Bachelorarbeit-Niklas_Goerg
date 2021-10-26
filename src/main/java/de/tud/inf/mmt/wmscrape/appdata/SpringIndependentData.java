package de.tud.inf.mmt.wmscrape.appdata;

public class SpringIndependentData {

    private static String username = "root";
    private static String password = "123456";
    private static String path = "jdbc:mysql://localhost/test_USER_DB";

    public SpringIndependentData() {}

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        SpringIndependentData.password = password;
    }

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        SpringIndependentData.path = path;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        SpringIndependentData.username = username;
    }

}