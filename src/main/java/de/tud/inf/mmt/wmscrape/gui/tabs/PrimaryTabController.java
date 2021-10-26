package de.tud.inf.mmt.wmscrape.gui.tabs;

import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PrimaryTabController {

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @FXML
    private void initialize() {
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
