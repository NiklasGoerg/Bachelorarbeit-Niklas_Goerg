package de.tud.inf.mmt.wmscrape;

import de.tud.inf.mmt.wmscrape.gui.GuiApplication;
import javafx.application.Application;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class WMScrape {

    public static void main(String[] args) {

        String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            System.setProperty("webdriver.gecko.driver","libs/geckodriver/mac_x86_64/geckodriver");
        } else if (OS.contains("win")) {
            System.setProperty("webdriver.gecko.driver","libs/geckodriver/win_64/geckodriver.exe");
        } else if (OS.contains("nux")) {
            System.setProperty("webdriver.gecko.driver","libs/geckodriver/nux_64/geckodriver");
        } else {
            throw new InvalidPropertyException(WMScrape.class,
                    "webdriver.gecko.driver", "Für dieses Betriebssystem steht keine Geckodriver bereit.");
        }
        

        Application.launch(GuiApplication.class, args);
    }
}