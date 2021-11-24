package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

import java.util.ArrayList;
import java.util.List;

// used to create the selection tree inside the scraping tab

public class WebsiteTree {
    private final TreeView<WebRepresentation<?>> treeView;
    private final ObservableMap<Website, ObservableList<WebsiteElement>> checkedItems;

    public WebsiteTree(List<Website> websites, ObservableMap<Website, ObservableList<WebsiteElement>> checkedItems) {
        treeView = new TreeView<>();
        this.checkedItems = checkedItems;

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

    public TreeView<WebRepresentation<?>> getTreeView() {
        return treeView;
    }

    private CheckBoxTreeItem<WebRepresentation<?>> createItem(WebRepresentation<?> object) {
        CheckBoxTreeItem<WebRepresentation<?>> item = new CheckBoxTreeItem<>(object);
        item.selectedProperty().addListener((o, ov, nv) -> updateSelected(nv, object));
        item.setExpanded(true);


        List<CheckBoxTreeItem<WebRepresentation<?>>> list = new ArrayList<>();
        for (WebRepresentation<?> webRepresentation : object.getChildren()) {

            // hide empty websites
            if(webRepresentation instanceof Website && webRepresentation.getChildren().isEmpty()) continue;

            CheckBoxTreeItem<WebRepresentation<?>> webRepresentationCheckBoxTreeItem = createItem(webRepresentation);
            list.add(webRepresentationCheckBoxTreeItem);
        }
        item.getChildren().addAll(list);


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
