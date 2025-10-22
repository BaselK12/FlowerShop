package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.session.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.ComplaintUpdatedBroadcast;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.UpdateComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.UpdateComplaintResponse;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.time.format.DateTimeFormatter;

/**
 * Complaint details view: allows editing resolution notes and changing status.
 */
public class ComplaintDetailsController {

    // Left panel
    @FXML private Label customerNameLabel;
    @FXML private Label statusChip;

    @FXML private Label complaintIdValue;
    @FXML private Label emailValue;
    @FXML private Label phoneValue;
    @FXML private Label orderIdValue;
    @FXML private Label storeNameValue;
    @FXML private Label createdAtValue;

    // Right panel
    @FXML private Label pageTitle;
    @FXML private Label subjectLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea resolutionArea;

    // Actions bar
    @FXML private Button startBtn;
    @FXML private Button resolveBtn;
    @FXML private Button rejectBtn;
    @FXML private Button saveBtn;
    @FXML private Button closeBtn;

    private Stage stage;
    private Complaint complaint;

    @FXML
    private void initialize() {
        // Ensure session hooks are installed and register to EventBus once.
        ClientSession.install();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Keep buttons enabled/disabled based on notes content and status.
        if (resolutionArea != null) {
            resolutionArea.textProperty().addListener((obs, o, n) -> updateButtonStates());
        }
        updateButtonStates();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        if (this.stage != null) {
            this.stage.setOnHidden(e -> safeUnregister());
        }
    }

    private void safeUnregister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /** Load complaint details into the UI */
    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
        refreshUIFromComplaint(complaint);
        applyEditingState();
        updateButtonStates();
    }

    private void refreshUIFromComplaint(Complaint c) {
        // top header
        customerNameLabel.setText("Customer #" + c.getCustomerId());
        statusChip.setText(c.getStatus() != null ? c.getStatus().toString() : "OPEN");
        statusChip.getStyleClass().setAll("status-chip", statusToStyle(c));

        // metadata left panel
        complaintIdValue.setText(String.valueOf(c.getId()));
        emailValue.setText("N/A");   // fill from customers table if/when available
        phoneValue.setText("N/A");
        orderIdValue.setText(c.getOrderId() != null ? String.valueOf(c.getOrderId()) : "N/A");
        storeNameValue.setText(c.getStoreId() != null ? ("Store #" + c.getStoreId()) : "N/A");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        createdAtValue.setText(c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "N/A");

        // right panel content
        pageTitle.setText("Complaint #" + c.getId());
        subjectLabel.setText(c.getSubject() != null ? c.getSubject() : "");
        descriptionArea.setText(c.getText() != null ? c.getText() : "");
        resolutionArea.setText(c.getResolution() != null ? c.getResolution() : "");
    }

    /** Map enum status to css style class */
    private String statusToStyle(Complaint complaint) {
        if (complaint.getStatus() == null) return "open";
        switch (complaint.getStatus()) {
            case OPEN: return "open";
            case IN_PROGRESS: return "in-progress";
            case RESOLVED: return "resolved";
            case REJECTED: return "rejected";
            default: return "open";
        }
    }

    /* ===== Actions (wired from FXML) ===== */

    @FXML
    private void onStart() {
        pushUpdate(null, Complaint.Status.IN_PROGRESS, false);
    }

    @FXML
    private void onResolve() {
        if (notesTrimmed().isBlank()) {
            warn("Please write resolution notes before resolving.");
            return;
        }
        pushUpdate(notesTrimmed(), Complaint.Status.RESOLVED, false);
    }

    @FXML
    private void onReject() {
        if (notesTrimmed().isBlank()) {
            warn("Please explain why you’re rejecting before changing the status.");
            return;
        }
        pushUpdate(notesTrimmed(), Complaint.Status.REJECTED, false);
    }

    @FXML
    private void onSaveNotes() {
        pushUpdate(notesTrimmed(), null, false);
    }

    @FXML
    private void handleClose() {
        safeUnregister();
        if (stage != null) stage.close();
    }

    /* ===== Client-server plumbing ===== */

    private void pushUpdate(String notes, Complaint.Status newStatus, boolean closeAfter) {
        if (complaint == null) return;
        try {
            String statusStr = newStatus == null ? null : newStatus.name();
            UpdateComplaintRequest req =
                    new UpdateComplaintRequest(complaint.getId(), notes, statusStr);
            SimpleClient.getClient().sendToServer(req);
            // optimistic UI tweaks if you want, but safer to wait for response
        } catch (Exception e) {
            e.printStackTrace();
            warn("Failed to send update: " + e.getMessage());
        }
    }

    /* ===== EventBus subscribers ===== */

    @Subscribe
    public void onServerMessage(ServerMessageEvent ev) {
        Object msg = ev.getPayload();
        if (msg instanceof UpdateComplaintResponse resp) {
            handleUpdateResponse(resp);
        } else if (msg instanceof ComplaintUpdatedBroadcast bc) {
            handleBroadcast(bc);
        }
    }


    private void handleUpdateResponse(UpdateComplaintResponse resp) {
        if (complaint == null) return;

        if (!resp.isOk()) {
            warn("Update failed: " + resp.getReason());
            return;
        }
        if (resp.getUpdated() == null) return;
        if (!resp.getUpdated().getId().equals(complaint.getId())) return;

        Platform.runLater(() -> {
            this.complaint = resp.getUpdated();
            refreshUIFromComplaint(this.complaint);
            applyEditingState();
            updateButtonStates();
        });
    }

    private void handleBroadcast(ComplaintUpdatedBroadcast bc) {
        if (complaint == null || bc.getComplaint() == null) return;
        if (!bc.getComplaint().getId().equals(complaint.getId())) return;

        Platform.runLater(() -> {
            this.complaint = bc.getComplaint();
            refreshUIFromComplaint(this.complaint);
            applyEditingState();
            updateButtonStates();
        });
    }

    /* ===== UI state helpers ===== */

    private void applyEditingState() {
        if (complaint == null) return;
        boolean isFinal = complaint.getStatus() == Complaint.Status.RESOLVED
                || complaint.getStatus() == Complaint.Status.REJECTED;

        // Editability
        resolutionArea.setEditable(!isFinal);

        // Buttons
        startBtn.setDisable(isFinal || complaint.getStatus() != Complaint.Status.OPEN);
        resolveBtn.setDisable(isFinal || notesTrimmed().isBlank());
        rejectBtn.setDisable(isFinal || notesTrimmed().isBlank());
        saveBtn.setDisable(isFinal);
    }

    private void updateButtonStates() {
        if (complaint == null) return;
        boolean isFinal = complaint.getStatus() == Complaint.Status.RESOLVED
                || complaint.getStatus() == Complaint.Status.REJECTED;

        if (resolveBtn != null) resolveBtn.setDisable(isFinal || notesTrimmed().isBlank());
        if (rejectBtn != null)  rejectBtn.setDisable(isFinal || notesTrimmed().isBlank());
        if (saveBtn != null)    saveBtn.setDisable(isFinal);
        if (startBtn != null)   startBtn.setDisable(isFinal || complaint.getStatus() != Complaint.Status.OPEN);
    }

    private String notesTrimmed() {
        String s = resolutionArea.getText();
        return s == null ? "" : s.trim();
    }

    /* ===== misc ===== */

    private void warn(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Complaint Update");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
