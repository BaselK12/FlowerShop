package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.greenrobot.eventbus.EventBus;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ManageComplainitsController {

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

        StatusCombo.setItems(FXCollections.observableArrayList("All", "Open", "In Progress", "Closed"));
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
                new ReadOnlyStringWrapper(safe(c.getValue().getStatus())));

        SummaryCol.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(safe(c.getValue().getSummary())));

        // 4) Buttons
        ApplyBtn.setOnAction(e -> requestComplaintsFromServer());
        BackBtn.setOnAction(e -> safeClose());
        CloseBtn.setOnAction(e -> safeClose());

        // 5) Initial load
        requestComplaintsFromServer();
    }







}

