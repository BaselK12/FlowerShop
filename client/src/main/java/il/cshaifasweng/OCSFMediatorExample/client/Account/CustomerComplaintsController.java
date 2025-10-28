package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.GetComplaintsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.GetCustomerComplaintsRequest;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CustomerComplaintsController {

    @FXML private TableView<Complaint> table;
    @FXML private TableColumn<Complaint, String> idCol, subjectCol, typeCol, statusCol, orderCol, createdCol;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Button FileComplaintBtn; // << new button

    private final ObservableList<Complaint> master = FXCollections.observableArrayList();
    private final ObservableList<Complaint> filtered = FXCollections.observableArrayList();

    private final DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        table.setItems(filtered);
        table.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });

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

        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        countLabel.setText("0 items");

        // Wire the button to open the complaint filing dialog
        if (FileComplaintBtn != null) {
            FileComplaintBtn.setOnAction(e -> openFilingDialog());
        }

        // Initial fetch
        refetch();
    }

    private void refetch() {
        long cid = ClientSession.getCustomerId();
        try {
            SimpleClient.getClient().sendToServer(new GetCustomerComplaintsRequest(cid));
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not request complaints: " + ex.getMessage()).showAndWait();
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
            new Alert(Alert.AlertType.ERROR, "Failed to open details: " + ex.getMessage()).showAndWait();
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
            // When the user closes the filing window, refresh the list
            st.setOnHidden(ev -> refetch());
            st.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to open filing form: " + ex.getMessage()).showAndWait();
        }
    }

    // -------- EventBus subscribers (cover all posting styles) --------

    // 1) If SimpleClient posts the raw message:
    @Subscribe
    public void onDirect(GetComplaintsResponse res) { handle(res); }

    // 2) If SimpleClient wraps it in client.events.ServerMessageEvent
    @Subscribe
    public void onServerMessage1(il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent ev) {
        Object msg = ev.getPayload();
        if (msg instanceof GetComplaintsResponse res) handle(res);
    }

    // 3) If SimpleClient wraps it in client.bus.events.ServerMessageEvent
    @Subscribe
    public void onServerMessage2(il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent ev) {
        Object msg = ev.getPayload();
        if (msg instanceof GetComplaintsResponse res) handle(res);
    }

    private void handle(GetComplaintsResponse res) {
        Platform.runLater(() -> {
            master.setAll(res.getComplaints());
            applyFilter();
            System.out.println("[CustomerComplaints] loaded " + res.getComplaints().size() + " rows");
        });
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
