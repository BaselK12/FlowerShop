package il.cshaifasweng.OCSFMediatorExample.client.Admin;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.*;

public class AdminDashboardController {

    // =================== FXML FIELDS ===================
    @FXML private TableView<FlowerDTO> flowersTable;
    @FXML private TableColumn<FlowerDTO, String> colFlowerName;
    @FXML private TableColumn<FlowerDTO, String> colFlowerPrice;
    @FXML private TableColumn<FlowerDTO, String> colFlowersCategory;




    @FXML private Button btnEditFlower;
    @FXML private Button btnDeleteFlower;
    @FXML private Button LogOutBtn;
    @FXML private Button reportsBtn;
    @FXML private Button employeesBtn;
    @FXML private Button ComplaintsBtn;

    @FXML private ScrollPane CenterStack;



    @FXML private VBox flowerSelectBox;
    @FXML private TableView<PromotionRow> pastPromoTable;
    @FXML private TableColumn<PromotionRow, String> colPromoTitle;
    @FXML private TableColumn<PromotionRow, String> colPromoStatus;
    @FXML private TableColumn<PromotionRow, String> colPromoDiscount;
    @FXML private TableColumn<PromotionRow, String> colPromoFlowers;
    @FXML private TableColumn<PromotionRow, String> colPromoPeriod;


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

        // Header buttons: wire navigation
        reportsBtn.setOnAction(e ->
                Nav.go(CenterStack, "/il/cshaifasweng/OCSFMediatorExample/client/ManageReports.fxml"));
        employeesBtn.setOnAction(e ->
                Nav.go(CenterStack, "/il/cshaifasweng/OCSFMediatorExample/client/Employee/ManageEmployees.fxml"));
        ComplaintsBtn.setOnAction(e ->
                Nav.go(CenterStack, "/il/cshaifasweng/OCSFMediatorExample/client/Complaint/ManageComplaints.fxml"));
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

    }

    private void setupPastPromotionsTable() {
        pastPromoTable.setItems(pastPromotions);
        colPromoTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colPromoStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().status()));
        colPromoDiscount.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().discount()));
        colPromoPeriod.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().period()));
    }

    private void wireButtons() {
        btnDeleteFlower.setOnAction(e -> onDeleteSelectedFlower());
        btnEditFlower.setOnAction(e -> onEditFlower());
    }

    // =================== FLOWER ACTIONS ===================
    private void onEditFlower() {
        FlowerDTO selected = flowersTable.getSelectionModel().getSelectedItem();
        openFlowerEditor(selected);
    }

    @FXML
    private void onAddFlower() {
        openFlowerEditor(null);
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

    // =================== RECORD FOR PROMOTIONS ===================
    public record PromotionRow(
            String title,
            String status,
            String discount,
            String period
    ) {
        static PromotionRow fromServerDto(PromotionDTO dto) {
            // Title and status
            String title = dto.getName();
            String status = dto.isActive() ? "Active" : "Expired";

            // Discount (format nicely)
            String discount;
            if ("PERCENT".equalsIgnoreCase(dto.getType())) {
                discount = dto.getAmount() + "%";
            } else {
                discount = String.format("$%.2f", dto.getAmount());
            }


            // Period (start–end)
            String period = (dto.getValidFrom() != null && dto.getValidTo() != null)
                    ? dto.getValidFrom() + " → " + dto.getValidTo()
                    : "—";

            return new PromotionRow(title, status, discount, period);
        }
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

        // Only show flowers without active promotions
        List<FlowerDTO> eligibleFlowers = flowers.stream()
                .filter(f -> !f.hasActivePromotion())
                .toList();

        if (eligibleFlowers.isEmpty()) {
            Label noneLabel = new Label("No available flowers (all have promotions)");
            noneLabel.getStyleClass().add("muted-label");
            flowerSelectBox.getChildren().add(noneLabel);
            return;
        }

        for (FlowerDTO f : eligibleFlowers) {
            CheckBox cb = new CheckBox(f.getName() + " — ₪" + String.format("%.2f", f.getPrice()));
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

    // =================== FLOWER EDITOR POPUP ===================
    private void openFlowerEditor(FlowerDTO flower) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Admin/addEditFlower.fxml"));
            Parent root = loader.load();

            AddEditFlowerController controller = loader.getController();
            controller.setFlower(flower);

            Stage stage = new Stage();
            stage.setTitle(flower == null ? "Add Flower" : "Edit Flower");
            stage.initModality(Modality.APPLICATION_MODAL); // make it modal
            stage.setScene(new Scene(root));
            stage.setMinWidth(500);
            stage.setMinHeight(700);

            root.getStylesheets().add(
                    getClass().getResource("/Styles/add-edit-flower.css").toExternalForm());

            stage.setOnHidden(evt -> {
                controller.onClose();
                requestFlowers(); // auto-refresh after closing editor
            });

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open flower editor: " + e.getMessage());
        }
    }


}
