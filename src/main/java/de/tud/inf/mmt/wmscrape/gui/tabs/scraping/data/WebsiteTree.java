package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// used to create the selection tree inside the scraping tab

public class WebsiteTree {
    private final TreeView<WebRepresentation<?>> treeView;
    private final ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems;
    private final Set<Integer> restoredSelected;

    public WebsiteTree(List<Website> websites, ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems,
                       Set<Integer> restoredSelected) {
        treeView = new TreeView<>();
        this.checkedItems = checkedItems;
        this.restoredSelected = restoredSelected;

        WebRepresentation<?> root = createRoot(websites);

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

        // set selected if hashcode was stored
        item.selectedProperty().addListener((o, ov, nv) -> updateSelected(nv, object));
        item.setExpanded(true);
        if(restoredSelected.contains(object.hashCode())) item.setSelected(true);

        List<CheckBoxTreeItem<WebRepresentation<?>>> list = new ArrayList<>();
        for (WebRepresentation<?> webRepresentation : object.getChildren()) {

            // hide empty websites
            if(webRepresentation instanceof Website && webRepresentation.getChildren().isEmpty()) continue;

            // recursive child creation
            CheckBoxTreeItem<WebRepresentation<?>> webRepresentationCheckBoxTreeItem = createItem(webRepresentation);
            list.add(webRepresentationCheckBoxTreeItem);
        }
        item.getChildren().addAll(list);


        return item;
    }

    private <T extends WebRepresentation<?>> void updateSelected(boolean selected, WebRepresentation<T> object) {

        if (selected) {
            storeSelected(object);
        } else {
            removeFromSelected(object);
        }
    }

    private <T extends WebRepresentation<?>> void removeFromSelected(WebRepresentation<T> object) {
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

    private <T extends WebRepresentation<?>> void storeSelected(WebRepresentation<T> object) {
        if (object instanceof Website && !checkedItems.containsKey(object)) {
            // add new website
            checkedItems.put((Website) object, FXCollections.observableSet());
        } else if (object instanceof WebsiteElement) {
            // add new website element and if not already done, create a new list
            var website = ((WebsiteElement) object).getWebsite();
            var list = checkedItems.getOrDefault(
                    website,
                    FXCollections.observableSet());
            list.add((WebsiteElement) object);
            checkedItems.put(website, list);
        }
    }

    private WebRepresentation<Website> createRoot(List<Website> websites) {
        return new WebRepresentation<>() {
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
    }

}
