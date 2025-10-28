package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Complaint;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class ComplaintDetailsReadOnlyController {

    @FXML private Label idLbl, emailLbl, phoneLbl, orderLbl, storeLbl, createdLbl;
    @FXML private Label subjectLbl, statusChip;
    @FXML private TextArea descriptionArea, resolutionArea;

    private final DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setComplaint(Complaint c) {
        idLbl.setText(String.valueOf(c.getId()));
        emailLbl.setText(nz(c.getEmail(), "N/A"));
        phoneLbl.setText(nz(c.getPhone(), "N/A"));
        orderLbl.setText(c.getOrderId() == null ? "N/A" : String.valueOf(c.getOrderId()));
        storeLbl.setText(c.getStoreId() == null ? "N/A" : ("Store #" + c.getStoreId()));
        createdLbl.setText(c.getCreatedAt() == null ? "N/A" : dt.format(c.getCreatedAt()));

        subjectLbl.setText(nz(c.getSubject(), ""));
        descriptionArea.setText(nz(c.getText(), ""));
        resolutionArea.setText(nz(c.getResolution(), ""));

        String st = c.getStatus() == null ? "OPEN" : c.getStatus().name();
        statusChip.setText(st.replace('_',' '));
        statusChip.getStyleClass().removeAll("open","in-progress","resolved","rejected");
        switch (st) {
            case "IN_PROGRESS" -> statusChip.getStyleClass().add("in-progress");
            case "RESOLVED"    -> statusChip.getStyleClass().add("resolved");
            case "REJECTED"    -> statusChip.getStyleClass().add("rejected");
            default            -> statusChip.getStyleClass().add("open");
        }
    }

    @FXML
    private void closeWindow() {
        Stage st = (Stage) statusChip.getScene().getWindow();
        if (st != null) st.close();
    }

    private static String nz(String s, String d) { return (s == null || s.isBlank()) ? d : s; }
}
