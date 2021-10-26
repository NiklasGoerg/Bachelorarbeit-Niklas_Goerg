package de.tud.inf.mmt.wmscrape;

import de.tud.inf.mmt.wmscrape.gui.GuiApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WMScrape {

    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
    }
}