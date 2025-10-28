package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ItemDetailsController {

    @FXML private Label modalTitle;
    @FXML private ImageView modalImage;
    @FXML private Label modalCategoryChip;
    @FXML private Label modalPrice;
    @FXML private Label modalDescription;
    @FXML private Button modalCloseBtn;
    @FXML private Button modalAddToCartBtn;

    private FlowerDTO item;
    private boolean loggedIn;

    /**
     * Populates modal with selected flower data.
     */
    public void setItem(FlowerDTO item, boolean loggedIn) {
        this.item = item;
        this.loggedIn = loggedIn;

        // Title
        modalTitle.setText(item.getName());

        // Description
        String desc = (item.getShortDescription() != null && !item.getShortDescription().isEmpty())
                ? item.getShortDescription()
                : "No description available.";
        modalDescription.setText(desc);

        // Categories
        List<String> categories = item.getCategories();
        if (categories != null && !categories.isEmpty()) {
            modalCategoryChip.setText(String.join(", ", categories));
        } else {
            modalCategoryChip.setText("Uncategorized");
        }

        // Price and promotion
        double price = item.getPrice();
        double effectivePrice = item.getEffectivePrice();

        if (item.getPromotion() != null && item.getPromotion().isActive() && effectivePrice < price) {
            modalPrice.setText(String.format("Price: $%.2f  (Now: $%.2f)", price, effectivePrice));

        } else {
            modalPrice.setText(String.format("Price: $%.2f", price));
        }

        // Image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                modalImage.setImage(new Image(item.getImageUrl(), true));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + item.getImageUrl());
                loadPlaceholder();
            }
        } else {
            loadPlaceholder();
        }

        // Disable AddToCart if not logged in
        modalAddToCartBtn.setDisable(!loggedIn);

        // Wire actions
        modalAddToCartBtn.setOnAction(e -> sendAddToCart());
        modalCloseBtn.setOnAction(e -> closeWindow());
    }

    private void loadPlaceholder() {
        try {
            modalImage.setImage(new Image(getClass().getResource("/images/placeholder.png").toExternalForm()));
        } catch (Exception ignored) {}
    }

    private void sendAddToCart() {
        if (item == null) return;
        try {
            App.getClient().sendToServer(new AddToCartRequest(item.getSku(), 1));
            System.out.println("Sent AddToCartRequest for sku=" + item.getSku());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) modalCloseBtn.getScene().getWindow();
        stage.close();
    }
}
