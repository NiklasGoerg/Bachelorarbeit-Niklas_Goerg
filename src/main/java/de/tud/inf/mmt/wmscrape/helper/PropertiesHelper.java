package de.tud.inf.mmt.wmscrape.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesHelper {
    private static final String propertiesPath = "src/main/resources/user.properties";

    public static void setProperty(String name, String value) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(propertiesPath));
            properties.setProperty(name, value);
            properties.store(new FileOutputStream(propertiesPath), null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    public static Map<String, String> getProperties(String... args) {
        Map<String, String> propertiesMap = new HashMap<>();
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(propertiesPath));

            for(var arg : args) {
                propertiesMap.put(arg, properties.getProperty(arg, null));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return propertiesMap;
    }

    public static String getProperty(String name, String defaultValue) {
        Properties properties = new Properties();
        String property = null;

        try {
            properties.load(new FileInputStream(propertiesPath));
            property = properties.getProperty(name, defaultValue);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return property;
    }
}
