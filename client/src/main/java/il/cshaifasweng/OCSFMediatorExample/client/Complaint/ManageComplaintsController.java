package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
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
import javafx.event.ActionEvent;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ManageComplaintsController {

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

    @FXML private CheckBox GroupBox;

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

    @FXML
    private void initialize() {
        // 1) EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // 2) Filters setup
        ScopeCombo.setItems(FXCollections.observableArrayList(SCOPE_WHOLE, SCOPE_STORE));
        ScopeCombo.getSelectionModel().select(SCOPE_WHOLE);

        // Store list — replace with a real fetch if you have one
        // You can request stores from server here and fill StoreCombo in a separate @Subscribe handler.
        StoreCombo.setItems(FXCollections.observableArrayList("Store A", "Store B", "Store C"));
        StoreCombo.setDisable(true); // enabled only when SCOPE_STORE is selected

        TypeCombo.setItems(FXCollections.observableArrayList(
                "All", "Service", "Product Quality", "Delivery", "Pricing", "Billing", "Refund", "Technical", "Other"
        ));
        TypeCombo.getSelectionModel().select("All");

        StatusCombo.setItems(FXCollections.observableArrayList("All", "OPEN", "IN_PROGRESS", "RESOLVED", "REJECTED"));
        StatusCombo.getSelectionModel().select("All");

        ScopeCombo.valueProperty().addListener((obs, oldV, newV) -> {
            boolean specific = Objects.equals(newV, SCOPE_STORE);
            StoreCombo.setDisable(!specific);
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
        BackBtn.setOnAction(e -> safeClose());
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
        //requestComplaintsFromServer();
        Platform.runLater(() -> requestComplaintsFromServer());
    }


    @FXML
    private void requestComplaintsFromServer(ActionEvent e) {
        requestComplaintsFromServer(); // call your existing no-arg method
    }

    @FXML
    private void safeClose(ActionEvent e) {
        safeClose(); // call your existing no-arg method
    }

    private void requestComplaintsFromServer() {
        if (App.getClient() == null || !App.getClient().isConnected()) {
            System.err.println("[WARN] Client not connected yet. Skipping complaints fetch.");
            return;
        }
        String scope = ScopeCombo.getValue();
        String store = ScopeCombo.getValue() != null && ScopeCombo.getValue().equals(SCOPE_STORE)
                ? StoreCombo.getValue()
                : null;
        String type = TypeCombo.getValue();
        String status = StatusCombo.getValue();

        // Build and send request; adjust constructor to your real message
        GetComplaintsRequest req = new GetComplaintsRequest(
                // Example fields — adapt to your request signature
                scopeEqualsStore(scope) ? Optional.ofNullable(store).orElse(null) : null,
                normalizeAll(type),
                normalizeAll(status)
        );

        try {
            App.getClient().sendToServer(req); // changed SimpleClient.getClient().sendToServer(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////// subscribe functions

//    @Subscribe
//    public void onGetComplaintsResponse(GetComplaintsResponse msg) {
//        Platform.runLater(() -> {
//            List<Complaint> list = msg.getComplaints();
//            rows.setAll(list);
//        });
//    }

    @Subscribe
    public void onGetComplaintsResponse(GetComplaintsResponse msg) {
        System.out.println("[CLIENT] Received complaints: " + msg.getComplaints().size());
        Platform.runLater(() -> {
            List<Complaint> list = msg.getComplaints();
            rows.setAll(list);
        });
    }



    @Subscribe
    public void onComplaintCreatedBroadcast(ComplaintCreatedBroadcast msg) {
        if (msg == null || msg.getComplaint() == null) return;
        Platform.runLater(() -> maybeAddIfMatchesFilters(msg.getComplaint()));
    }

    private void maybeAddIfMatchesFilters(Complaint c) {
        if (!matchesActiveFilters(c)) return;
        // Avoid duplicates (by id)
        boolean exists = rows.stream().anyMatch(r -> Objects.equals(r.getId(), c.getId()));
        if (!exists) {
            rows.add(0, c); // add to top so user sees it immediately
            // Optionally resort: ComplaintsTable.sort();
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
            // Adjust to your entity’s date field:
            // If you have LocalDateTime getCreatedAt():
            return c.getCreatedAt() != null ? c.getCreatedAt().format(dateFmt) : "";
            // If it’s java.util.Date, convert first:
            // return c.getCreatedAt() != null ? dateFmt.format(c.getCreatedAt()
            //        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void safeClose() {
        // Unregister and close window
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        try {
            CloseBtn.getScene().getWindow().hide();
        } catch (Exception ignored) {}
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
                    getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/ComplaintDetails.fxml")
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






}

