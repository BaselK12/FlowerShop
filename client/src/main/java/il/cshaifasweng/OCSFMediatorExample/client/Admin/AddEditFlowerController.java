package il.cshaifasweng.OCSFMediatorExample.client.Admin;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.SaveFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.CategoryDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCategoriesRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCategoriesResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddEditFlowerController {

    // ====== FXML Nodes ======
    @FXML private VBox fromBox;
    @FXML private VBox fieldBox;
    @FXML private TextField skuField;
    @FXML private TextField nameField;
    @FXML private TextField shortDescriptionField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private ListView<CategoryDTO> categoriesList;
    @FXML private HBox imagePickerRow;
    @FXML private Button btnChooseImage;
    @FXML private Label choosenFileLabel;
    @FXML private Label selectedCategoriesLabel;
    @FXML private StackPane imagePreviewBox;
    @FXML private ImageView previewImage;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // ====== Internal state ======
    private File selectedImageFile;
    private static final String PLACEHOLDER =
            "https://via.placeholder.com/300x180/efebce/5a4e4e?text=No+Image";
    private FlowerDTO flower;
    private boolean registered = false;
    private boolean categoriesLoaded = false;

    // ====== Initialization ======
    @FXML
    private void initialize() {
        if (!registered) {
            EventBus.getDefault().register(this);
            registered = true;
        }

        previewImage.setImage(new Image(PLACEHOLDER));
        categoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Ask server for category list
        try {
            App.getClient().sendSafely(new GetCategoriesRequest());
        } catch (Exception e) {
            showError("Failed to request categories: " + e.getMessage());
        }

        // Pretty display for category list
        categoriesList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CategoryDTO category, boolean empty) {
                super.updateItem(category, empty);
                setText(empty || category == null
                        ? null
                        : (category.getDisplayName() != null ? category.getDisplayName() : category.getName()));
            }
        });

        // Update label live when user selects/deselects categories
        categoriesList.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<CategoryDTO>) change -> updateSelectedCategoriesLabel()
        );

        fromBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && flower != null) {
                Platform.runLater(this::restoreBasicFields);
            }
        });
    }

    // ====== Image Picker ======
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

    // ====== Save ======
    @FXML
    private void onSave() {
        String sku = skuField.getText().trim();
        String name = nameField.getText().trim();
        String shortDesc = shortDescriptionField.getText().trim();
        String longDesc = descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();

        if (sku.isEmpty() || name.isEmpty() || priceText.isEmpty()) {
            showError("Please fill all required fields (SKU, Name, Price).");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showError("Invalid price format. Please enter a numeric value.");
            return;
        }

        // Use full description if available, otherwise short one
        String desc = !longDesc.isEmpty() ? longDesc : shortDesc;

        List<String> selectedCategoryNames = categoriesList.getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(CategoryDTO::getName)
                .collect(Collectors.toList());

        String imageUrl = selectedImageFile != null
                ? "images/" + selectedImageFile.getName()
                : (flower != null ? flower.getImageUrl() : null);

        SaveFlowerRequest.ActionType action =
                (flower == null)
                        ? SaveFlowerRequest.ActionType.ADD
                        : SaveFlowerRequest.ActionType.EDIT;

        SaveFlowerRequest req = new SaveFlowerRequest(
                action,
                sku,
                name,
                desc,
                price,
                imageUrl,
                selectedCategoryNames
        );

        try {
            App.getClient().sendSafely(req);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Flower Sent");
            info.setHeaderText("Request sent to server");
            info.setContentText("Waiting for server response...");
            info.showAndWait();

        } catch (IOException e) {
            showError("Failed to send flower to server: " + e.getMessage());
        }
    }

    // ====== Cancel ======
    @FXML
    private void onCancel() {
        if (flower != null) restoreOriginalValues();
        else clearForm();
    }

    // ====== Helpers ======
    private void clearForm() {
        skuField.clear();
        nameField.clear();
        shortDescriptionField.clear();
        descriptionArea.clear();
        priceField.clear();
        choosenFileLabel.setText("No File Chosen");
        previewImage.setImage(new Image(PLACEHOLDER));
        selectedImageFile = null;
        categoriesList.getSelectionModel().clearSelection();
        updateSelectedCategoriesLabel();
    }

    private void restoreOriginalValues() {
        skuField.setText(flower.getSku());
        nameField.setText(flower.getName());
        shortDescriptionField.setText(flower.getShortDescription());
        descriptionArea.setText(flower.getDescription());
        priceField.setText(String.valueOf(flower.getPrice()));

        String img = flower.getImageUrl();
        if (img != null && !img.isEmpty()) {
            previewImage.setImage(new Image(img, true));
            choosenFileLabel.setText(img);
        } else {
            previewImage.setImage(new Image(PLACEHOLDER));
            choosenFileLabel.setText("No File Chosen");
        }

        skuField.setDisable(true);

        // restore selected categories
        categoriesList.getSelectionModel().clearSelection();
        if (flower.getCategories() != null && !flower.getCategories().isEmpty()) {
            for (String c : flower.getCategories()) {
                for (CategoryDTO dto : categoriesList.getItems()) {
                    String display = dto.getDisplayName() != null ? dto.getDisplayName().trim().toLowerCase() : "";
                    String internal = dto.getName() != null ? dto.getName().trim().toLowerCase() : "";
                    String target = c.trim().toLowerCase();

                    if (display.equals(target) || internal.equals(target)) {
                        categoriesList.getSelectionModel().select(dto);
                        break;
                    }
                }
            }
        }

        // scroll to first selected
        if (!categoriesList.getSelectionModel().getSelectedIndices().isEmpty()) {
            categoriesList.scrollTo(categoriesList.getSelectionModel().getSelectedIndices().get(0));
        }

        System.out.println("Restoring categories for " + flower.getName() + ": " + flower.getCategories());
        System.out.println("Available categories: " + categoriesList.getItems().stream().map(CategoryDTO::getName).toList());

        updateSelectedCategoriesLabel();
    }

    private void updateSelectedCategoriesLabel() {
        List<CategoryDTO> selected = categoriesList.getSelectionModel().getSelectedItems();

        if (selected == null || selected.isEmpty()) {
            selectedCategoriesLabel.setText("Selected: None");
        } else {
            String joined = selected.stream()
                    .map(c -> c.getDisplayName() != null ? c.getDisplayName() : c.getName())
                    .collect(Collectors.joining(", "));
            selectedCategoriesLabel.setText("Selected: " + joined);
        }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    // ====== EventBus response ======
    @Subscribe
    public void onCategoriesReceived(GetCategoriesResponse response) {
        Platform.runLater(() -> {
            if (response.getCategories() == null || response.getCategories().isEmpty()) {
                showError("No categories received from the server.");
                return;
            }

            categoriesList.setItems(FXCollections.observableArrayList(response.getCategories()));
            categoriesLoaded = true;

            // MANUAL SELECTION from FlowerDTO
            if (flower != null && flower.getCategories() != null) {
                categoriesList.getSelectionModel().clearSelection();

                for (String catName : flower.getCategories()) {
                    for (CategoryDTO dto : response.getCategories()) {
                        if (dto.getName().equalsIgnoreCase(catName) ||
                                (dto.getDisplayName() != null && dto.getDisplayName().equalsIgnoreCase(catName))) {
                            categoriesList.getSelectionModel().select(dto);
                            break;
                        }
                    }
                }
            }

            updateSelectedCategoriesLabel();
        });
    }

    // ====== Setter for Edit Mode ======
    public void setFlower(FlowerDTO flower) {
        this.flower = flower;

        if (flower != null) {
            btnSave.setText("Save Changes");
            // If the Scene is already attached, fill now; otherwise the listener in initialize() will run it.
            if (fromBox.getScene() != null) {
                Platform.runLater(this::restoreBasicFields);
            }
        } else {
            clearForm();
            skuField.setDisable(false);
            btnSave.setText("Add Flower");
        }
    }

    private void restoreBasicFields() {
        if (flower == null) return;

        skuField.setText(flower.getSku());
        nameField.setText(flower.getName());
        shortDescriptionField.setText(flower.getShortDescription());
        descriptionArea.setText(flower.getDescription());
        priceField.setText(String.valueOf(flower.getPrice()));

        String img = flower.getImageUrl();
        if (img != null && !img.isEmpty()) {
            previewImage.setImage(new Image(img, true));
            choosenFileLabel.setText(img);
        } else {
            previewImage.setImage(new Image(PLACEHOLDER));
            choosenFileLabel.setText("No File Chosen");
        }

        skuField.setDisable(true);
    }
    // ====== Cleanup ======
    public void onClose() {
        if (registered) {
            EventBus.getDefault().unregister(this);
            registered = false;
        }
    }
}