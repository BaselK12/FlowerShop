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
import org.greenrobot.eventbus.ThreadMode;

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

    // Prevent double-sends; also used to gate buttons while a request is in flight
    private volatile boolean sending = false;

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
        if (c == null) return;

        // top header
        if (customerNameLabel != null) customerNameLabel.setText("Customer #" + c.getCustomerId());
        if (statusChip != null) {
            statusChip.setText(c.getStatus() != null ? c.getStatus().toString() : "OPEN");
            statusChip.getStyleClass().setAll("status-chip", statusToStyle(c));
        }

        // metadata left panel
        if (complaintIdValue != null) complaintIdValue.setText(String.valueOf(c.getId()));
        if (emailValue != null)   emailValue.setText("N/A");   // fill from customers table if/when available
        if (phoneValue != null)   phoneValue.setText("N/A");
        if (orderIdValue != null) orderIdValue.setText(c.getOrderId() != null ? String.valueOf(c.getOrderId()) : "N/A");
        if (storeNameValue != null) storeNameValue.setText(c.getStoreId() != null ? ("Store #" + c.getStoreId()) : "N/A");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (createdAtValue != null) createdAtValue.setText(c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "N/A");

        // right panel content
        if (pageTitle != null)    pageTitle.setText("Complaint #" + c.getId());
        if (subjectLabel != null) subjectLabel.setText(c.getSubject() != null ? c.getSubject() : "");
        if (descriptionArea != null) descriptionArea.setText(c.getText() != null ? c.getText() : "");
        if (resolutionArea != null)  resolutionArea.setText(c.getResolution() != null ? c.getResolution() : "");
    }

    /** Map enum status to css style class */
    private String statusToStyle(Complaint complaint) {
        if (complaint == null || complaint.getStatus() == null) return "open";
        switch (complaint.getStatus()) {
            case OPEN:        return "open";
            case IN_PROGRESS: return "in-progress";
            case RESOLVED:    return "resolved";
            case REJECTED:    return "rejected";
            default:          return "open";
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
        if (complaint == null || sending) return;
        try {
            sending = true;
            updateButtonStates(); // lock buttons while sending

            String statusStr = newStatus == null ? null : newStatus.name();
            UpdateComplaintRequest req =
                    new UpdateComplaintRequest(complaint.getId(), notes, statusStr);
            SimpleClient.getClient().sendToServer(req);
            // optimistic UI can be added here; we wait for server ack
        } catch (Exception e) {
            e.printStackTrace();
            sending = false;
            updateButtonStates();
            warn("Failed to send update: " + e.getMessage());
        }
    }

    /* ===== EventBus subscribers ===== */

    // Wrapper event (your SimpleClient may post this)
    @Subscribe
    public void onServerMessage(ServerMessageEvent ev) {
        Object msg = ev.getPayload();
        if (msg instanceof UpdateComplaintResponse resp) {
            handleUpdateResponse(resp);
        } else if (msg instanceof ComplaintUpdatedBroadcast bc) {
            handleBroadcast(bc);
        }
    }

    // Direct messages (your SimpleClient may also post these directly)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateComplaintResponse(UpdateComplaintResponse resp) {
        handleUpdateResponse(resp);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onComplaintUpdatedBroadcast(ComplaintUpdatedBroadcast bc) {
        handleBroadcast(bc);
    }

    private void handleUpdateResponse(UpdateComplaintResponse resp) {
        if (complaint == null || resp == null) {
            sending = false;
            updateButtonStates();
            return;
        }
        if (!resp.isOk()) {
            sending = false;
            updateButtonStates();
            warn("Update failed: " + resp.getReason());
            return;
        }
        if (resp.getUpdated() == null || !resp.getUpdated().getId().equals(complaint.getId())) {
            sending = false;
            updateButtonStates();
            return;
        }

        Platform.runLater(() -> {
            this.complaint = resp.getUpdated();
            refreshUIFromComplaint(this.complaint);
            applyEditingState();
            sending = false;
            updateButtonStates();
        });
    }

    private void handleBroadcast(ComplaintUpdatedBroadcast bc) {
        if (complaint == null || bc == null || bc.getComplaint() == null) return;
        if (!bc.getComplaint().getId().equals(complaint.getId())) return;

        Platform.runLater(() -> {
            this.complaint = bc.getComplaint();
            refreshUIFromComplaint(this.complaint);
            applyEditingState();
            // do not force sending=false here; broadcast may arrive unrelated to our own send
            updateButtonStates();
        });
    }

    /* ===== UI state helpers ===== */

    private void applyEditingState() {
        if (complaint == null) return;
        boolean isFinal = complaint.getStatus() == Complaint.Status.RESOLVED
                || complaint.getStatus() == Complaint.Status.REJECTED;

        // Editability
        if (resolutionArea != null) resolutionArea.setEditable(!isFinal);

        // Buttons (base state; fine-tuned in updateButtonStates)
        if (startBtn != null)   startBtn.setDisable(isFinal || complaint.getStatus() != Complaint.Status.OPEN);
        if (saveBtn != null)    saveBtn.setDisable(isFinal);
        if (resolveBtn != null) resolveBtn.setDisable(isFinal || notesTrimmed().isBlank());
        if (rejectBtn != null)  rejectBtn.setDisable(isFinal || notesTrimmed().isBlank());
    }

    private void updateButtonStates() {
        if (complaint == null) return;

        boolean isFinal = complaint.getStatus() == Complaint.Status.RESOLVED
                || complaint.getStatus() == Complaint.Status.REJECTED;

        // During sending, lock all action buttons
        boolean lock = sending;

        if (resolveBtn != null) resolveBtn.setDisable(lock || isFinal || notesTrimmed().isBlank());
        if (rejectBtn != null)  rejectBtn.setDisable(lock || isFinal || notesTrimmed().isBlank());
        if (saveBtn != null)    saveBtn.setDisable(lock || isFinal);
        if (startBtn != null)   startBtn.setDisable(lock || isFinal || complaint.getStatus() != Complaint.Status.OPEN);
    }

    private String notesTrimmed() {
        String s = resolutionArea != null ? resolutionArea.getText() : null;
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
