package il.cshaifasweng.OCSFMediatorExample.client.Complaint;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class ComplaintDetailsController {

    @FXML private Label customerNameLabel;
    @FXML private Label statusChip;

    @FXML private Label complaintIdValue;
    @FXML private Label emailValue;
    @FXML private Label phoneValue;
    @FXML private Label orderIdValue;
    @FXML private Label storeNameValue;
    @FXML private Label createdAtValue;

    @FXML private Label pageTitle;
    @FXML private Label subjectLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea resolutionArea;

    private Stage stage;

    /** Load complaint details into the UI */
    public void setComplaint(Complaint complaint) {
        // top header
        customerNameLabel.setText("Customer #" + complaint.getCustomerId());
        statusChip.setText(complaint.getStatus().toString());

        // switch chip color class
        statusChip.getStyleClass().setAll("status-chip", statusToStyle(complaint));

        // metadata left panel
        complaintIdValue.setText(String.valueOf(complaint.getId()));
        emailValue.setText("N/A");   // you can fetch from customers table if available
        phoneValue.setText("N/A");   // same as above
        orderIdValue.setText(String.valueOf(complaint.getOrderId()));
        storeNameValue.setText("Store #" + complaint.getStoreId());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        createdAtValue.setText(complaint.getCreatedAt() != null ? complaint.getCreatedAt().format(fmt) : "N/A");

        // right panel content
        pageTitle.setText("Complaint #" + complaint.getId());
        subjectLabel.setText(complaint.getSubject());
        descriptionArea.setText(complaint.getText());
        resolutionArea.setText(complaint.getResolution() != null ? complaint.getResolution() : "");
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}
