package il.cshaifasweng.OCSFMediatorExample.client.Admin;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.*;

public class AdminDashboardController {

    // =================== FXML FIELDS ===================
    @FXML private TableView<FlowerDTO> flowersTable;
    @FXML private TableColumn<FlowerDTO, String> colFlowerName;
    @FXML private TableColumn<FlowerDTO, String> colFlowerPrice;
    @FXML private TableColumn<FlowerDTO, String> colFlowersCategory;
    @FXML private TableColumn<FlowerDTO, Void> colFlowerActions;
    @FXML private Button btnDeleteFlower;
    @FXML private VBox flowerSelectBox;
    @FXML private TableView<PromotionRow> pastPromoTable;
    @FXML private TableColumn<PromotionRow, String> colPromoTitle;
    @FXML private TableColumn<PromotionRow, String> colPromoStatus;

    // =================== DATA ===================
    private final ObservableList<FlowerDTO> flowers = FXCollections.observableArrayList();
    private final ObservableList<PromotionRow> pastPromotions = FXCollections.observableArrayList();
    private final Map<String, CheckBox> checkboxBySku = new LinkedHashMap<>();

    // =================== INITIALIZATION ===================
    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        setupFlowersTable();
        setupPastPromotionsTable();
        wireButtons();

        requestFlowers();
        requestPromotions();
    }

    // =================== SETUP HELPERS ===================

    private void setupFlowersTable() {
        flowersTable.setItems(flowers);

        colFlowerName.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(safe(c.getValue().getName())));
        colFlowerPrice.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(String.format("%.2f", c.getValue().getPrice())));
        colFlowersCategory.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(joinCategories(c.getValue().getCategories())));

        // Add action buttons to each row
        colFlowerActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = makeSmallBtn("Edit");
            private final Button deleteBtn = makeSmallBtn("Delete");
            private final HBox box = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-outline");

                editBtn.setOnAction(e -> {
                    FlowerDTO item = getTableView().getItems().get(getIndex());
                    onEditFlower(item);
                });

                deleteBtn.setOnAction(e -> {
                    FlowerDTO item = getTableView().getItems().get(getIndex());
                    onDeleteFlower(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupPastPromotionsTable() {
        pastPromoTable.setItems(pastPromotions);
        colPromoTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colPromoStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().status()));
    }

    private void wireButtons() {
        btnDeleteFlower.setOnAction(e -> onDeleteSelectedFlower());
    }

    // =================== FLOWER ACTIONS ===================

    private void onEditFlower(FlowerDTO flower) {
        showInfo("Edit Flower", "Open edit view for " + flower.getName());
    }

    private void onDeleteFlower(FlowerDTO flower) {
        if (!confirm("Delete Flower",
                "Are you sure you want to delete " + flower.getName() + "?")) return;

        try {
            App.getClient().sendToServer(new DeleteFlowerRequest(flower.getSku()));
        } catch (Exception e) {
            showError("Error", "Failed to send delete request: " + e.getMessage());
        }
    }

    private void onDeleteSelectedFlower() {
        FlowerDTO selected = flowersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a flower to delete.");
            return;
        }
        onDeleteFlower(selected);
    }

    // =================== EVENTBUS RESPONSES ===================

    @Subscribe
    public void onGetFlowersResponse(GetCatalogResponse res) {
        Platform.runLater(() -> {
            flowers.setAll(res.getFlowers());
            rebuildFlowerCheckboxes();
        });
    }

    @Subscribe
    public void onGetPromotionsResponse(GetPromotionsResponse res) {
        Platform.runLater(() -> {
            pastPromotions.setAll(res.getPromotions().stream()
                    .map(PromotionRow::fromServerDto)
                    .toList());
        });
    }

    @Subscribe
    public void onDeleteFlowerResponse(DeleteFlowerResponse resp) {
        Platform.runLater(() -> {
            if (!resp.isSuccess()) {
                showError("Delete Failed", resp.getError());
            } else {
                showInfo("Deleted", "Flower was successfully removed.");
                requestFlowers(); // refresh list
            }
        });
    }

    // =================== REQUEST HELPERS ===================

    private void requestFlowers() {
        try {
            App.getClient().sendSafely(new GetCatalogRequest(null, null, null, false));
        } catch (Exception e) {
            showError("Error", "Failed to fetch flowers: " + e.getMessage());
        }
    }

    private void requestPromotions() {
        try {
            App.getClient().sendToServer(new GetPromotionsRequest());
        } catch (Exception e) {
            showError("Error", "Failed to fetch promotions: " + e.getMessage());
        }
    }

    // =================== UI UTILITIES ===================

    private Button makeSmallBtn(String text) {
        Button b = new Button(text);
        b.setMinWidth(60);
        b.setPrefHeight(26);
        return b;
    }

    private void rebuildFlowerCheckboxes() {
        checkboxBySku.clear();
        flowerSelectBox.getChildren().clear();
        for (FlowerDTO f : flowers) {
            CheckBox cb = new CheckBox(f.getName());
            flowerSelectBox.getChildren().add(cb);
            checkboxBySku.put(f.getSku(), cb);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private String joinCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) return "";
        return String.join(", ", categories);
    }

    // =================== POPUP HELPERS ===================

    private void showInfo(String title, String content) {
        showPopup(title, content, Alert.AlertType.INFORMATION);
    }

    private void showError(String title, String content) {
        showPopup(title, content, Alert.AlertType.ERROR);
    }

    private boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showPopup(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // =================== RECORD FOR PROMOTIONS ===================

    public record PromotionRow(String title, String status) {
        static PromotionRow fromServerDto(Object dto) {
            return new PromotionRow("Demo Promo", "Active");
        }
    }
}
