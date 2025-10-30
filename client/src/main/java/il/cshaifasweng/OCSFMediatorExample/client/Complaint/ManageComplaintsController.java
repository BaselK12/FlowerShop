package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresError;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.StoreOption;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ManageComplaintsController {

    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminDashboard.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }

    @FXML private Button ApplyBtn;
    @FXML private Button BackBtn;
    @FXML private Button CloseBtn;

    @FXML private TableView<Complaint> ComplaintsTable;
    @FXML private TableColumn<Complaint, String> DateCol;
    @FXML private TableColumn<Complaint, String> IDCol;
    @FXML private TableColumn<Complaint, String> SummaryCol;
    @FXML private TableColumn<Complaint, String> TypeCol;
    @FXML private TableColumn<Complaint, String> StatusCol;
    @FXML private TableColumn<Complaint, String> StoreCol;

    @FXML private CheckBox GroupBox; // (kept if used in FXML)

    @FXML private ComboBox<String> ScopeCombo;
    @FXML private ComboBox<String> StatusCombo;
    @FXML private ComboBox<String> StoreCombo;
    @FXML private ComboBox<String> TypeCombo;

    // Data
    private final ObservableList<Complaint> rows = FXCollections.observableArrayList();

    // Formatting
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Constants
    private static final String SCOPE_WHOLE = "Whole Company";
    private static final String SCOPE_STORE = "Specific Store";
    private final Map<String, Long> storeNameToId = new HashMap<>();


    @FXML
    private void initialize() {
        // 1) EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // 2) Filters setup
        ScopeCombo.setItems(FXCollections.observableArrayList(SCOPE_WHOLE, SCOPE_STORE));
        ScopeCombo.getSelectionModel().select(SCOPE_WHOLE);

        StoreCombo.setItems(FXCollections.observableArrayList());
        StoreCombo.setDisable(true); // enabled only when SCOPE_STORE is selected

        TypeCombo.setItems(FXCollections.observableArrayList(
                "All", "Service", "Product Quality", "Delivery", "Pricing", "Billing", "Refund", "Technical", "Other"
        ));
        TypeCombo.getSelectionModel().select("All");

        StatusCombo.setItems(FXCollections.observableArrayList("All", "OPEN", "IN_PROGRESS", "RESOLVED", "REJECTED"));
        StatusCombo.getSelectionModel().select("All");

        ScopeCombo.valueProperty().addListener((obs, oldV, newV) -> {
            boolean specific = Objects.equals(newV, SCOPE_STORE);
            boolean hasStores = StoreCombo.getItems() != null && !StoreCombo.getItems().isEmpty();
            StoreCombo.setDisable(!specific || !hasStores);
        });

        // 3) Table setup
        ComplaintsTable.setItems(rows);
        ComplaintsTable.setPlaceholder(new Label("No complaints found"));

        IDCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(String.valueOf(c.getValue().getId())));

        StoreCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(safe(c.getValue().getStoreName())));

        DateCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(formatDateSafe(c.getValue())));

        TypeCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(safe(c.getValue().getType())));

        StatusCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(safe(c.getValue().getStatusName())));

        SummaryCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(safe(c.getValue().getSubject())));

        // 4) Buttons
        ApplyBtn.setOnAction(e -> requestComplaintsFromServer());
        BackBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Back clicked -> " + returnToFxml);
            cleanup();
            Nav.go(BackBtn, returnToFxml);
        });
        CloseBtn.setOnAction(e -> safeClose());

        ComplaintsTable.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Complaint selectedComplaint = row.getItem();
                    openComplaintDetails(selectedComplaint);
                }
            });
            return row;
        });

        // 5) Initial load
        Platform.runLater(() -> {
            requestStoresFromServer();
            requestComplaintsFromServer();
        });
    }

    /* ===== UI handlers bound in FXML (keep) ===== */

    @FXML
    private void requestComplaintsFromServer(ActionEvent e) {
        requestComplaintsFromServer();
    }

    @FXML
    private void safeClose(ActionEvent e) {
        safeClose();
    }

    /* ===== Requests ===== */

    private void requestComplaintsFromServer() {
        var client = App.getClient();
        if (client == null || !client.isConnected()) {
            System.err.println("[WARN] Client not connected yet. Skipping complaints fetch.");
            return;
        }

        String scope = ScopeCombo.getValue();
        String type = TypeCombo.getValue();
        String status = StatusCombo.getValue();

        String storeName = scopeEqualsStore(scope) ? StoreCombo.getValue() : null;
        Long storeId = (storeName != null) ? storeNameToId.get(storeName) : null;

        GetComplaintsRequest req = new GetComplaintsRequest(
                scopeEqualsStore(scope) ? storeId : null,
                normalizeAll(type),
                normalizeAll(status)
        );

        try {
            client.sendToServer(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestStoresFromServer() {
        var client = App.getClient();
        if (client == null || !client.isConnected()) {
            System.err.println("[WARN] Client not connected yet. Skipping stores fetch.");
            return;
        }
        try {
            SimpleClient.getClient().sendToServer(new GetStoresRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===== Subscriptions: direct ===== */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetComplaintsResponse(GetComplaintsResponse msg) {
        if (msg == null || msg.getComplaints() == null) return;
        System.out.println("[CLIENT] Received complaints: " + msg.getComplaints().size());
        rows.setAll(msg.getComplaints());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onComplaintCreatedBroadcast(ComplaintCreatedBroadcast msg) {
        if (msg == null || msg.getComplaint() == null) return;
        maybeAddIfMatchesFilters(msg.getComplaint());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStoresResponse(GetStoresResponse resp) {
        if (resp == null || resp.stores == null) return;

        storeNameToId.clear(); // reset old data

        ObservableList<String> storeNames = FXCollections.observableArrayList();
        for (StoreOption option : resp.stores) {
            if (option != null && option.name != null && !option.name.isBlank()) {
                storeNames.add(option.name);
                try {
                    // convert String → Long safely when inserting
                    Long idLong = Long.parseLong(option.id);
                    storeNameToId.put(option.name, idLong);
                } catch (NumberFormatException e) {
                    System.err.println("[WARN] invalid store ID format: " + option.id);
                }
            }
        }

        StoreCombo.getSelectionModel().clearSelection();
        StoreCombo.setItems(storeNames);

        boolean specific = scopeEqualsStore(ScopeCombo.getValue());
        StoreCombo.setDisable(!specific || storeNames.isEmpty());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStoresError(GetStoresError err) {
        if (err == null) return;
        showError(err.message != null ? err.message : "Failed to load stores.");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateResponse(UpdateComplaintResponse msg) {
        if (msg == null) return;
        if (msg.isOk() && msg.getUpdated() != null) {
            refreshOrReplaceRow(msg.getUpdated());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBroadcast(ComplaintUpdatedBroadcast msg) {
        if (msg == null || msg.getComplaint() == null) return;
        refreshOrReplaceRow(msg.getComplaint());
    }

    /* ===== Subscriptions: wrapped in ServerMessageEvent ===== */

    @Subscribe
    public void onServerMessage(ServerMessageEvent ev) {
        if (ev == null) return;
        Object p = ev.getPayload();
        if (p instanceof GetComplaintsResponse r1) {
            Platform.runLater(() -> onGetComplaintsResponse(r1));
        } else if (p instanceof ComplaintCreatedBroadcast r2) {
            Platform.runLater(() -> onComplaintCreatedBroadcast(r2));
        } else if (p instanceof UpdateComplaintResponse r3) {
            Platform.runLater(() -> onUpdateResponse(r3));
        } else if (p instanceof ComplaintUpdatedBroadcast r4) {
            Platform.runLater(() -> onBroadcast(r4));
        } else if (p instanceof GetStoresResponse r5) {
            Platform.runLater(() -> onStoresResponse(r5));
        } else if (p instanceof GetStoresError r6) {
            Platform.runLater(() -> onStoresError(r6));
        }
    }

    /* ===== Helpers ===== */

    private void maybeAddIfMatchesFilters(Complaint c) {
        if (!matchesActiveFilters(c)) return;
        boolean exists = rows.stream().anyMatch(r -> Objects.equals(r.getId(), c.getId()));
        if (!exists) {
            rows.add(0, c); // add to top so user sees it immediately
            // ComplaintsTable.sort(); // optional
        }
    }

    private boolean matchesActiveFilters(Complaint c) {
        String scope = ScopeCombo.getValue();
        String store = StoreCombo.getValue();
        String type = TypeCombo.getValue();
        String status = StatusCombo.getValue();

        if (scopeEqualsStore(scope)) {
            if (store == null || store.isBlank()) return false;
            if (!store.equalsIgnoreCase(safe(c.getStoreName()))) return false;
        }
        if (!"All".equalsIgnoreCase(safe(type))) {
            if (!safe(type).equalsIgnoreCase(safe(c.getType()))) return false;
        }
        if (!"All".equalsIgnoreCase(safe(status))) {
            if (!safe(status).equalsIgnoreCase(safe(c.getStatusName()))) return false;
        }
        return true;
    }

    private boolean scopeEqualsStore(String scope) {
        return Objects.equals(scope, SCOPE_STORE);
    }

    private String normalizeAll(String v) {
        if (v == null) return null;
        return "All".equalsIgnoreCase(v) ? null : v;
    }

    private String formatDateSafe(Complaint c) {
        try {
            return c.getCreatedAt() != null ? c.getCreatedAt().format(dateFmt) : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void safeClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        try {
            if (CloseBtn != null && CloseBtn.getScene() != null) {
                CloseBtn.getScene().getWindow().hide();
            }
        } catch (Exception ignored) { }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            a.setHeaderText("Error");
            a.showAndWait();
        });
    }

    // on double click to see the full details
    private void openComplaintDetails(Complaint complaint) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Complaint/ComplaintDetails.fxml")
            );
            Parent root = loader.load();

            ComplaintDetailsController controller = loader.getController();
            controller.setComplaint(complaint);

            Stage stage = new Stage();
            stage.setTitle("Complaint Details");
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshOrReplaceRow(Complaint c) {
        if (c == null) return;
        Platform.runLater(() -> {
            for (int i = 0; i < ComplaintsTable.getItems().size(); i++) {
                var row = ComplaintsTable.getItems().get(i);
                if (Objects.equals(row.getId(), c.getId())) {
                    ComplaintsTable.getItems().set(i, c);
                    ComplaintsTable.refresh();
                    return;
                }
            }
            if (matchesActiveFilters(c)) {
                ComplaintsTable.getItems().add(0, c);
            }
        });
    }

    private void cleanup() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            System.out.println("[RegisterUI] EventBus unregistered");
        }
    }
}
