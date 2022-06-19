package de.tud.inf.mmt.wmscrape.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * class to simplify and centralize the usage of user.properties file
 */

public class PropertiesHelper {
    private static final String propertiesPath = "src/main/resources/user.properties";

    /**
     * saves or updates a property in the user.properties file
     *
     * @param name of the property to save
     * @param value of the property}
     */
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

    /**
     *
     * @param name of the property
     * @return value of the property
     */
    public static String getProperty(String name) {
        return getProperty(name, null);
    }

    /**
     * get multiple properties with one call
     * useful for bulk readings since the file is only opened and read once
     *
     * @param args names of the properties to read
     * @return a map with the values of the retrieved properties, the names of the properties are the keys
     */
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

    /**
     * reads a property from user.properties file with a fallback value if property isn't present in thr file
     *
     * @param name of the property
     * @param defaultValue fallback value if the actual value can't be read
     * @return the value of the property or the defaultValue in an error case
     */
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
