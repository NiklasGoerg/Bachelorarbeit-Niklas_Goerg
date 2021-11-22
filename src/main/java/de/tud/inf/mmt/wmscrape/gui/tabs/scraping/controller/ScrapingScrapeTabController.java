package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebRepresentation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.WebsiteRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Controller
public class ScrapingScrapeTabController {

    //@FXML private TreeView<WebRepresentation<?>> selectionTree;
    @FXML private TextArea logArea;
    @FXML private Spinner<Double> delayMinSpinner;
    @FXML private Spinner<Double> delayMaxSpinner;
    @FXML private Spinner<Double> waitSpinner;
    @FXML private CheckBox headlessCheckBox;
    @FXML private CheckBox pauseCheckBox;
    @FXML private BorderPane borderPane;

    private ObservableMap<Website, ObservableList<WebsiteElement>> checkedItems = FXCollections.observableMap(new HashMap<>());

    //TODO remove
    @Autowired
    DataSource dataSource;

    @Autowired
    private WebsiteRepository websiteRepository;

    private static SimpleStringProperty logText;

    @FXML
    private void initialize() {
        logText = new SimpleStringProperty("");
        logArea.clear();
        logArea.textProperty().bind(logText);

        delayMinSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10, 1, 0.25));
        delayMaxSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 50, 3, 0.25));
        waitSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 50, 5, 0.25));
        headlessCheckBox.setSelected(false);
        pauseCheckBox.setSelected(false);
        borderPane.setCenter(test().treeView);
    }

    // TODO move to manager
    @Transactional
    public websiteTree test() {
        List<Website> root = new ArrayList<>(websiteRepository.findAll());
        return new websiteTree(root);
    }

    @FXML
    @Transactional
    public void handleStartButton() {
        logText.set("");
        Optional<Website> website = fresh();

        if(website.isEmpty()) return;

        try {
            // TODO close datasource
            WebsiteScraper scraper = new WebsiteScraper(website.get(), logText, false, dataSource.getConnection());
            scraper.processWebsite();
            scraper.quit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Optional<Website> fresh() {
        return websiteRepository.findByDescription("localhost");
    }

    @FXML
    private void handleNextButton() {
    }


    public class websiteTree {
        private final TreeView<WebRepresentation<?>> treeView;

        public websiteTree(List<Website> websites) {
            treeView = new TreeView<>();

            WebRepresentation<?> root = new WebRepresentation<Website>() {
                @Override
                public String getDescription() {
                    return "root";
                }

                @Override
                public void setDescription(String description) {
                }

                @Override
                public List<Website> getChildren() {
                    return websites;
                }
            };

            TreeItem<WebRepresentation<?>> treeRoot = createItem(root);
            treeView.setRoot(treeRoot);
            treeView.setShowRoot(false);
            treeView.setCellFactory(tv -> new CheckBoxTreeCell<>() {
                @Override
                public void updateItem(WebRepresentation<?> item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) textProperty().set(item.getDescription());
                }
            });
        }

        private CheckBoxTreeItem<WebRepresentation<?>> createItem(WebRepresentation<?> object) {
            CheckBoxTreeItem<WebRepresentation<?>> item = new CheckBoxTreeItem<>(object);
            item.selectedProperty().addListener((o, ov, nv) -> updateSelected(nv, object));
            item.setExpanded(true);
            item.getChildren().addAll(object.getChildren().stream().map(this::createItem).collect(toList()));
            return item;
        }

        private <T extends WebRepresentation<?>> void updateSelected(boolean selected, WebRepresentation<T> object) {

            if (selected) {
                if (object instanceof Website && !checkedItems.containsKey(object)) {
                    // add new website
                    checkedItems.put((Website) object, FXCollections.observableArrayList());
                } else if (object instanceof WebsiteElement) {
                    // add new website element and if not already done, create a new list
                    var website = ((WebsiteElement) object).getWebsite();
                    var list = checkedItems.getOrDefault(
                            website,
                            FXCollections.observableArrayList());
                    list.add((WebsiteElement) object);
                    checkedItems.put(website, list);
                }
            } else {
                if (object instanceof Website && checkedItems.containsKey(object)) {
                    // remove website
                    // if no website elements are selected this is also executed
                    var website = checkedItems.get((Website) object);
                    if (website != null) checkedItems.remove((Website) object);

                } else if (object instanceof WebsiteElement) {
                    // remove website element
                    var elements = checkedItems.get(((WebsiteElement) object).getWebsite());
                    if (elements != null) elements.remove((WebsiteElement) object);
                }
            }
        }
    }

}
