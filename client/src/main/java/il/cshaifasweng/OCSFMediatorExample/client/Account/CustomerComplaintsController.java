package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.ViewTracker;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.GetComplaintsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.GetCustomerComplaintsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CustomerComplaintsController {

    @FXML private TableView<Complaint> table;
    @FXML private TableColumn<Complaint, String> idCol, subjectCol, typeCol, statusCol, orderCol, createdCol;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Button FileComplaintBtn; // "File a complaint"

    private final ObservableList<Complaint> master   = FXCollections.observableArrayList();
    private final ObservableList<Complaint> filtered = FXCollections.observableArrayList();

    private final DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Table wiring
        if (table != null) {
            table.setItems(filtered);
            table.setPlaceholder(new Label("No complaints yet."));
            table.setRowFactory(tv -> {
                TableRow<Complaint> row = new TableRow<>();
                row.setOnMouseClicked(e -> {
                    if (!row.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                        openDetails(row.getItem());
                    }
                });
                return row;
            });
        }

        idCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getId())));
        subjectCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getSubject())));
        typeCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getType())));
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getStatus() == null ? "" : c.getValue().getStatus().name().replace('_', ' ')
        ));
        orderCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getOrderId() == null ? "" : String.valueOf(c.getValue().getOrderId())
        ));
        createdCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt() == null ? "" : dt.format(c.getValue().getCreatedAt())
        ));

        // Search
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        countLabel.setText("0 items");

        // Button
        if (FileComplaintBtn != null) {
            FileComplaintBtn.setOnAction(e -> openFilingDialog());
            FileComplaintBtn.setDisable(ClientSession.getCustomerId() <= 0);
        }

        // Initial fetch only if logged in
        if (ClientSession.getCustomerId() > 0) {
            refetch();
        } else if (table != null) {
            table.setPlaceholder(new Label("Log in to view your complaints."));
        }
    }

    private void refetch() {
        long cid = ClientSession.getCustomerId();
        if (cid <= 0) return; // don’t spam server anonymously
        try {
            SimpleClient.getClient().sendToServer(new GetCustomerComplaintsRequest(cid));
        } catch (IOException ex) {
            ex.printStackTrace();
            alertErr("Could not request complaints: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        filtered.setAll(master.filtered(c ->
                q.isEmpty()
                        || (c.getSubject() != null && c.getSubject().toLowerCase(Locale.ROOT).contains(q))
                        || (c.getType() != null && c.getType().toLowerCase(Locale.ROOT).contains(q))
                        || (c.getStatus() != null && c.getStatus().name().toLowerCase(Locale.ROOT).contains(q))
                        || (c.getOrderId() != null && String.valueOf(c.getOrderId()).contains(q))
        ));
        countLabel.setText(filtered.size() + " items");
    }

    private void openDetails(Complaint c) {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Account/ComplaintDetailsReadOnly.fxml"));
            Parent root = fx.load();
            ComplaintDetailsReadOnlyController ctrl = fx.getController();
            ctrl.setComplaint(c);

            Stage st = new Stage();
            st.setTitle("Complaint #" + c.getId());
            st.initOwner(table.getScene().getWindow());
            st.initModality(Modality.WINDOW_MODAL);
            st.setScene(new Scene(root));
            st.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alertErr("Failed to open details: " + ex.getMessage());
        }
    }

    private void openFilingDialog() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Complaint/FilingComplaint.fxml"));
            Parent root = fx.load();

            Stage st = new Stage();
            st.setTitle("File a Complaint");
            st.initOwner(table.getScene().getWindow());
            st.initModality(Modality.WINDOW_MODAL);
            st.setScene(new Scene(root));
            st.setOnHidden(ev -> refetch()); // refresh after filing
            st.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alertErr("Failed to open filing form: " + ex.getMessage());
        }
    }

    // -------- EventBus subscribers --------

    // Raw message
    @Subscribe
    public void onDirect(GetComplaintsResponse res) { handle(res); }

    // Wrapped message (keep ONE of these)
    @Subscribe
    public void onServerMessage(il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent ev) {
        Object msg = ev.getPayload();
        if (msg instanceof GetComplaintsResponse res) handle(res);
    }

    // Refresh when login succeeds
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        if (FileComplaintBtn != null) FileComplaintBtn.setDisable(false);
        refetch();
    }

    // Refresh when overview hydrates the customer
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        if (FileComplaintBtn != null) FileComplaintBtn.setDisable(false);
        refetch();
    }

    // Optional: when this view becomes active and nothing loaded yet, fetch
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActive(ViewTracker.ActiveControllerChanged e) {
        if (e == null) return;
        String id = e.controllerId != null ? e.controllerId : e.getControllerId();
        if ("CustomerComplaints".equals(id) && master.isEmpty() && ClientSession.getCustomerId() > 0) {
            refetch();
        }
    }

    private void handle(GetComplaintsResponse res) {
        Platform.runLater(() -> {
            master.setAll(res.getComplaints());
            applyFilter();
            System.out.println("[CustomerComplaints] loaded " + res.getComplaints().size() + " rows");
        });
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static void alertErr(String msg) {
        new Alert(AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
