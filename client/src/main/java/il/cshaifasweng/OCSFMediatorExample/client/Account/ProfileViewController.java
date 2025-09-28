package il.cshaifasweng.OCSFMediatorExample.client.Account;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class ProfileViewController {

    // Profile view mode
    @FXML private VBox ProfileBox;
    @FXML private AnchorPane cardProfileView;
    @FXML private Label NameLabel;
    @FXML private Label EmailLabel;
    @FXML private Label PhoneLabel;
    @FXML private Button EditBtn;

    // Edit mode
    @FXML private AnchorPane CardProfileEditPane;
    @FXML private TextField NameField;
    @FXML private TextField EmailField;
    @FXML private TextField PhoneEmail;  // maybe rename to PhoneField for clarity
    @FXML private Button CancelBtn;
    @FXML private Button SaveBtn;

    // Optional: keep user profile data in memory
    private String currentName = "John Doe";
    private String currentEmail = "john@example.com";
    private String currentPhone = "+972-50-1234567";

    @FXML
    private void initialize() {
        // Initialize with sample data or load from server
        updateViewLabels();

        // Wire button actions
        EditBtn.setOnAction(e -> switchToEdit());
        CancelBtn.setOnAction(e -> cancelEdit());
        SaveBtn.setOnAction(e -> saveProfile());
    }

    private void updateViewLabels() {
        NameLabel.setText(currentName);
        EmailLabel.setText(currentEmail);
        PhoneLabel.setText(currentPhone);
    }

    private void switchToEdit() {
        // Fill text fields with current values
        NameField.setText(currentName);
        EmailField.setText(currentEmail);
        PhoneEmail.setText(currentPhone);

        // Show edit pane, hide view card
        cardProfileView.setVisible(false);
        CardProfileEditPane.setVisible(true);
    }

    private void cancelEdit() {
        // Just flip back without saving
        CardProfileEditPane.setVisible(false);
        cardProfileView.setVisible(true);
    }

    private void saveProfile() {
        // Save new values
        currentName = NameField.getText();
        currentEmail = EmailField.getText();
        currentPhone = PhoneEmail.getText();

        // Update labels
        updateViewLabels();

        // TODO: send updated profile to server via SimpleClient

        // Flip back to view mode
        CardProfileEditPane.setVisible(false);
        cardProfileView.setVisible(true);
    }
}
