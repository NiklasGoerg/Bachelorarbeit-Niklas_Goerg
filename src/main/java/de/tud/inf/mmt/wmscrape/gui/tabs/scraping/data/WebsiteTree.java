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

/**
 * used to create the selection tree inside the scraping tab
 */
public class WebsiteTree {
    private final TreeView<WebRepresentation<?>> treeView;
    private final ObservableMap<Website, ObservableSet<WebsiteElement>> checkedItems;
    private final Set<Integer> restoredSelected;

    /**
     * creates the tree which can be accessed by {@link #getTreeView()}
     *
     * @param websites all website configurations to be added
     * @param checkedItems the observable list where selected elements will be stored in
     * @param restoredSelected the hash values of the previously selected elements restored from user.properties
     */
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

    /**
     * recursively builds the tree by accessing {@link WebRepresentation#getChildren()}
     *
     * @param object the parent node
     * @return one checkbox item (at the end it's the root item containing all sub items)
     */
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

    /**
     * adds or removes an element from the list of selected elemetns
     *
     * @param selected if true the element is added
     * @param object the object where the selection has been changed
     * @param <T> the subtype e.g. {@link Website} or {@link WebsiteElement}
     */
    private <T extends WebRepresentation<?>> void updateSelected(boolean selected, WebRepresentation<T> object) {

        if (selected) {
            storeSelected(object);
        } else {
            removeFromSelected(object);
        }
    }

    /**
     * called by {@link #updateSelected(boolean, WebRepresentation)} to remove an element
     *
     * @param object the object where the selection has been changed
     * @param <T> the subtype e.g. {@link Website} or {@link WebsiteElement}
     */
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

    /**
     * called by {@link #updateSelected(boolean, WebRepresentation)} to add an element
     *
     * @param object the object where the selection has been changed
     * @param <T> the subtype e.g. {@link Website} or {@link WebsiteElement}
     */
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

    /**
     * creates an hidden pseudo root
     * @param websites the children of the root node are all website configurations
     * @return the root node
     */
    private WebRepresentation<Website> createRoot(List<Website> websites) {
        return new WebRepresentation<>() {
            @Override
            public String getDescription() {
                return "root";
            }

            @Override
            public List<Website> getChildren() {
                return websites;
            }
        };
    }

}
