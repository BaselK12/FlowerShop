package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetPromotionsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetPromotionsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersResponse;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class AdminDashboardController {

    // =================== FXML FIELDS ===================
    @FXML private HBox headerBar;
    @FXML private Button btnLogout;

    @FXML private TableView<FlowerDTO> flowersTable;
    @FXML private TableColumn<FlowerDTO, String> colFlowerName;
    @FXML private TableColumn<FlowerDTO, String> colFlowerPrice;
    @FXML private TableColumn<FlowerDTO, String> colFlowersCategory;
    @FXML private TableColumn<FlowerDTO, Void> colFlowerActions;

    @FXML private Button btnAddFlower;
    @FXML private Button btnEditFlower;
    @FXML private Button btnDeleteFlower;

    @FXML private VBox flowerSelectBox;
    @FXML private TextField txtPromoTitle;
    @FXML private TextField txtPromoDiscription;
    @FXML private TextField txtPromoDiscount;
    @FXML private DatePicker dpPromoStart;
    @FXML private DatePicker dpPromoEnd;
    @FXML private Button btnSendPromotion;

    @FXML private TableView<PromotionRow> pastPromoTable;
    @FXML private TableColumn<PromotionRow, String> colPromoTitle;
    @FXML private TableColumn<PromotionRow, String> colPromoFlowers;
    @FXML private TableColumn<PromotionRow, String> colPromoDiscount;
    @FXML private TableColumn<PromotionRow, String> colPromoPeriod;
    @FXML private TableColumn<PromotionRow, String> colPromoStatus;
    @FXML private TableColumn<PromotionRow, PromotionRow> colPromoActions;

    // =================== DATA ===================
    private final ObservableList<FlowerDTO> flowers = FXCollections.observableArrayList();
    private final ObservableList<PromotionRow> pastPromotions = FXCollections.observableArrayList();
    private final Map<String, CheckBox> checkboxBySku = new LinkedHashMap<>();
    private final Set<String> selectedSkus = new LinkedHashSet<>();

    // =================== INITIALIZATION ===================
    @FXML
    private void initialize() {
        // called automatically after FXML load

        // EventBus
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        setupFlowersTable();
        setupPastPromotionsTable();
        wireButtons();

//        SimpleClient.getClient().sendToServer(new GetFlowersRequest());
//        SimpleClient.getClient().sendToServer(new GetPromotionsRequest());
    }

    // =================== SETUP HELPERS ===================

    private void setupFlowersTable() {
        flowersTable.setItems(flowers);
        colFlowerName.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(safe(c.getValue().getName())));
        colFlowerPrice.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(String.format("%.2f", c.getValue().getPrice())));
//        colFlowersCategory.setCellValueFactory(c ->
//                new ReadOnlyObjectWrapper<>(safe(c.getValue().getCategoryName())));
        // ... (actions column factory same as before)
    }

    private void setupPastPromotionsTable() {
        pastPromoTable.setItems(pastPromotions);
        colPromoTitle.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().title()));
        colPromoStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : s);
                getStyleClass().removeAll("status-active", "status-expired");
                if (!empty) {
                    getStyleClass().add("Active".equalsIgnoreCase(s) ? "status-active" : "status-expired");
                }
            }
        });
    }

    private void wireButtons() {
//        btnLogout.setOnAction(e -> onLogout());
//        btnAddFlower.setOnAction(e -> onAddFlower());
//        btnEditFlower.setOnAction(e -> onEditSelectedFlower());
//        btnDeleteFlower.setOnAction(e -> onDeleteSelectedFlower());
        btnSendPromotion.setOnAction(e -> onSendPromotion());
    }

    // =================== EVENTBUS RESPONSES ===================

    @Subscribe
    public void onGetFlowersResponse(GetFlowersResponse res) {
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

    // =================== PROMOTION CREATION ===================

    private void onSendPromotion() {
        // same logic: validate, collect data, send CreatePromotionRequest
    }

    private void rebuildFlowerCheckboxes() {
        checkboxBySku.clear();
        flowerSelectBox.getChildren().clear();
        for (FlowerDTO f : flowers) {
            CheckBox cb = new CheckBox(f.getName());
            flowerSelectBox.getChildren().add(cb);
        }
    }

    // =================== CLEANUP ===================
    public void dispose() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    // =================== UTILITIES ===================
    private static String safe(String s) { return s == null ? "" : s; }

    public record PromotionRow(String title, String status) {
        static PromotionRow fromServerDto(Object dto) {
            return new PromotionRow("Demo Promo", "Active");
        }
    }
}
