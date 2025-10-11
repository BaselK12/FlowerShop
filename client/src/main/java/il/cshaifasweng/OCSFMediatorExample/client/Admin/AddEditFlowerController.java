package il.cshaifasweng.OCSFMediatorExample.client.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AddEditFlowerController {

    // ====== FXML Nodes ======
    @FXML private VBox fromBox;
    @FXML private VBox fieldBox;
    @FXML private TextField skuField;
    @FXML private TextField nameField;         // add this in FXML!
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private HBox imagePickerRow;
    @FXML private Button btnChooseImage;
    @FXML private Label choosenFileLabel;
    @FXML private StackPane imagePreviewBox;
    @FXML private ImageView previewImage;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // ====== Internal state ======
    private File selectedImageFile;
    private static final String PLACEHOLDER =
            "https://via.placeholder.com/300x180/efebce/5a4e4e?text=No+Image";

    // ====== Event Handlers ======
    @FXML
    private void initialize() {
        previewImage.setImage(new Image(PLACEHOLDER));
    }

    @FXML
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Flower Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = chooser.showOpenDialog(fromBox.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            choosenFileLabel.setText(file.getName());
            try (FileInputStream fis = new FileInputStream(file)) {
                previewImage.setImage(new Image(fis));
            } catch (IOException e) {
                showError("Could not load image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onSave() {
        String sku = skuField.getText().trim();
        String name = nameField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String price = priceField.getText().trim();

        if (sku.isEmpty() || name.isEmpty() || price.isEmpty()) {
            showError("Please fill all required fields (SKU, Name, Price).");
            return;
        }

        // Example success feedback — replace with your real send logic
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Flower Saved");
        alert.setHeaderText("Saved Successfully");
        alert.setContentText(
                "SKU: " + sku + "\n" +
                        "Name: " + name + "\n" +
                        "Price: ₪" + price + "\n" +
                        "Description: " + desc + "\n" +
                        "Image: " + (selectedImageFile != null ? selectedImageFile.getName() : "None")
        );
        alert.showAndWait();

        clearForm();
    }

    @FXML
    private void onCancel() {
        clearForm();
    }

    // ====== Helper Methods ======
    private void clearForm() {
        skuField.clear();
        nameField.clear();
        descriptionArea.clear();
        priceField.clear();
        choosenFileLabel.setText("No File Chosen");
        previewImage.setImage(new Image(PLACEHOLDER));
        selectedImageFile = null;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}