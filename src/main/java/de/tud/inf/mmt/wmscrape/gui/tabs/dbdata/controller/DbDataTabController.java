package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.PrimaryTabManagement;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class DbDataTabController {
    @FXML private TabPane dataSubTabPane;
    @Autowired private StockTabController stockTabController;
    @Autowired private PrimaryTabManagement primaryTabManagement;

    @FXML
    private void initialize() throws IOException {
        Parent parent = primaryTabManagement.loadTabFxml("gui/tabs/dbdata/controller/stockTab.fxml", stockTabController);
        Tab tab = new Tab("Stamm", parent);
        dataSubTabPane.getTabs().add(tab);
    }

}
