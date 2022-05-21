package de.tud.inf.mmt.wmscrape.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesHelper {
    public static void setProperty(String name, String value) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            properties.setProperty(name, value);
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    public static String getProperty(String name, String defaultValue) {
        Properties properties = new Properties();
        String property = null;

        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
            property = properties.getProperty(name, defaultValue);
            properties.store(new FileOutputStream("src/main/resources/user.properties"), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return property;
    }
}
