package com.alegd.materialfx;

import com.alegd.materialfx.dataload.DataProvider;
import com.alegd.materialfx.dataload.Exportable;
import com.alegd.materialfx.pagination.JFXPagination;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToolbar;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author J. Alejandro Guerra Denis
 */
public abstract class DataContainer<T> extends BorderPane {

    private ObjectProperty<JFXToolbar> header;

    public final ObjectProperty<JFXToolbar> headerProperty() {
        if (header == null) {
            header = new SimpleObjectProperty<>();
        }

        return header;
    }

    private ObjectProperty<JFXToolbar> contextualHeader;

    public final ObjectProperty<JFXToolbar> contextualHeaderProperty() {
        if (contextualHeader == null) {
            JFXToolbar toolbar = new JFXToolbar();
            toolbar.getStyleClass().add("table-header");
            toolbar.getStyleClass().add("contextual-table-header");
            contextualHeader = new SimpleObjectProperty<>(toolbar);
            contextualHeaderTitle = new Label();
            contextualHeader.get().setLeftItems(contextualHeaderTitle);
        }

        return contextualHeader;
    }

    private Label contextualHeaderTitle;

    protected Exportable exportable;

    protected DataProvider dataProvider;

    protected VBox mEmptyState;

    boolean showHeaderWithNoData;

    protected ObservableList<T> viewHolders;

    protected ListProperty<Selectable> selectedItems;

    protected boolean showPages;

    protected JFXPagination pagination;

    private ResourceBundle resources;


    protected DataContainer() {
        resources = ResourceBundle.getBundle("i8n.material-fx");

        if (viewHolders == null)
            viewHolders = FXCollections.observableArrayList();

        if (selectedItems == null)
            selectedItems = new SimpleListProperty<>(FXCollections.observableArrayList());

        selectedItems.addListener((observable, oldValue, newValue) -> {
            if (newValue.size() > 0) {
                setTop(contextualHeaderProperty().get());
                String secondArg = (newValue.size() > 1) ? "s" : "";

                String message = MessageFormat.format(resources.getString("selected_item"),
                        newValue.size(), secondArg);
                contextualHeaderTitle.setText(message);
            } else {
                setTop(header.get());
            }
        });
    }


    /**
     *
     */
    public synchronized void loadData() {
        dataProvider.setupDataContainer();
        viewHolders.clear();
        dataProvider.loadData();

        if (mEmptyState != null)
            checkContent();
    }

    /**
     *
     */
    public synchronized void paginate() {
        if (viewHolders.size() > 10 && showPages) {
            pagination = new JFXPagination(getData().size(), 0);
            pagination.setPageFactory(this::createPage);
            setCenter(pagination);
        }
    }

    /**
     * Add a header (toolbar) to data table. Usually a data table header has a title at the
     * most left side and one or more actions (normally {@link Button}) at the most right side.
     *
     * @param mainTitle The title in the header for the data table
     * @param actions   The actions in the header for the data table
     */
    public synchronized void withHeader(String mainTitle, Node... actions) {
        if (exportable != null) {
            JFXButton printButton = new JFXButton(null,
                    Constant.getIcon("print", 20, Color.GRAY));
            printButton.setTooltip(new Tooltip(resources.getString("print")));
            printButton.setOnMouseClicked(event -> exportable.printData());

            JFXButton exportButton = new JFXButton(null,
                    Constant.getIcon("file_download", 18, Color.GRAY));
            exportButton.setTooltip(new Tooltip(resources.getString("export_data")));
            exportButton.setOnMouseClicked(event -> exportable.exportData());

            List<Node> moreActions = new ArrayList<>();
            moreActions.add(printButton);
            moreActions.add(exportButton);
            moreActions.addAll(Arrays.asList(actions));
            getHeader().setRightItems((Node[]) moreActions.toArray(new Node[actions.length + 1]));
        } else {
            getHeader().setRightItems(actions);
        }

        setTop(getHeader());
    }

    /**
     * Add an empty state to the data table. Empty states occur when an item’s content can’t
     * be shown for any reason.
     *
     * @param title    The title for the empty state card
     * @param subtitle The subtitle for the empty state card, usually a suggestion to get
     *                 rid of the emptiness.
     */
    public synchronized void withEmptyState(String title, String subtitle, boolean keepHeader) {
        showHeaderWithNoData = keepHeader;
        withEmptyState(title, subtitle);
    }

    /**
     * Add an empty state to the data table. Empty states occur when an item’s content can’t
     * be shown for any reason.
     *
     * @param title    The title for the empty state card
     * @param subtitle The subtitle for the empty state card, usually a suggestion to get
     *                 rid of the emptiness.
     */
    public synchronized void withEmptyState(String title, String subtitle) {
        Label titleLabel = new Label(resources.getString("no_data_title"));
        titleLabel.getStyleClass().add("main-text");

        Label subtitleLabel = new Label("");
        subtitleLabel.getStyleClass().add("suggestion-text");

        if (title != null)
            titleLabel.setText(title);

        if (subtitle != null)
            subtitleLabel.setText(subtitle);

        mEmptyState = new VBox(8, titleLabel, subtitleLabel);
        mEmptyState.getStyleClass().add("empty-state");
        mEmptyState.setAlignment(Pos.CENTER);
    }


    protected abstract Node createPage(int pageIndex);


    public abstract void checkContent();


    public abstract void addSelectionBoxes();


    public synchronized boolean add(T item) {
        return viewHolders.add(item);
    }


    public ObservableList<T> getData() {
        return viewHolders;
    }


    public void setViewHolders(ObservableList<T> viewHolders) {
        this.viewHolders = viewHolders;
    }


    public ObservableList<Selectable> getSelectedItems() {
        return selectedItems.get();
    }


    public final JFXToolbar getHeader() {
        return headerProperty().get();
    }


    public void setHeader(JFXToolbar toolbar) {
        toolbar.getStyleClass().add("table-header");
        this.headerProperty().set(toolbar);
        setTop(header.get());
    }


    public JFXToolbar getContextualHeader() {
        return contextualHeaderProperty().get();
    }


    public void setContextualHeader(JFXToolbar toolbar) {
        this.headerProperty().set(toolbar);
        setTop(contextualHeader.get());
    }


    public void setShowHeaderWithNoData(boolean showHeader) {
        this.showHeaderWithNoData = showHeader;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public boolean havePages() {
        return showPages;
    }

    public void showInPages(boolean showPages) {
        this.showPages = showPages;
    }

    public abstract void removeSelectionBox();


    public void setSelectableItems(boolean selectableItems) {
        if (selectableItems)
            addSelectionBoxes();
        else
            removeSelectionBox();
    }
}
