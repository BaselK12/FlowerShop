package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Category;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class ItemCardController {

    @FXML private StackPane itemCardRoot;
    @FXML private ImageView productImage;
    @FXML private Label promoRibbon;
    @FXML private Label categoryChip;
    @FXML private Label nameLabel;
    @FXML private Label shortDescLabel;
    @FXML private Label priceLabel;
    @FXML private Button addToCartBtn;
    @FXML private Button detailsBtn;

    private FlowerDTO item;
    private Runnable onDetails;
    private boolean loggedIn;

    // ============================
    // Set data for this card
    // ============================
    public void setData(FlowerDTO item, boolean loggedIn, Runnable onDetails) {
        this.item = item;
        this.loggedIn = loggedIn;
        this.onDetails = onDetails;

        if (addToCartBtn != null) {
            addToCartBtn.setId("addToCart");
        }

        // Product name
        nameLabel.setText(item.getName());

        // Short description
        if (item.getShortDescription() != null && !item.getShortDescription().isEmpty()) {
            shortDescLabel.setText(item.getShortDescription());
        } else {
            shortDescLabel.setText("");
        }

        // Categories
        List<String> cats = item.getCategories();
        if (cats != null && !cats.isEmpty()) {
            categoryChip.setText(String.join(", ", cats));
        } else {
            categoryChip.setText("Uncategorized");
        }

        double price = item.getPrice();
        double effectivePrice = item.getEffectivePrice();
        boolean hasActivePromo = item.getPromotion() != null && Boolean.TRUE.equals(item.getPromotion().isActive());

        if (hasActivePromo && effectivePrice < price) {
            priceLabel.setText(String.format("$%.2f  (Now: $%.2f)", price, effectivePrice));
            if (promoRibbon != null) {
                promoRibbon.setText(item.getPromotion().getName());
                promoRibbon.setVisible(true);
            }
        } else {
            priceLabel.setText(String.format("$%.2f", price));
            if (promoRibbon != null) {
                promoRibbon.setVisible(false);
            }
        }

        // Image
        if (productImage != null && item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                productImage.setImage(new Image(item.getImageUrl(), true));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + item.getImageUrl());
                productImage.setImage(null);
            }
        }

        // Login state
        addToCartBtn.setDisable(!loggedIn);

        // Events
        addToCartBtn.setOnAction(e -> sendAddToCart());
        detailsBtn.setOnAction(e -> openDetails());
        itemCardRoot.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) openDetails();
        });
    }

    // ============================
    // Add to cart logic
    // ============================
    private void sendAddToCart() {
        if (item == null) return;

        try {
            SimpleClient.getClient().sendToServer(new AddToCartRequest(item.getSku(), 1));
            System.out.println("Sent AddToCartRequest for sku=" + item.getSku());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Optional: allow parent to toggle login state without reloading the card. */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        if (addToCartBtn != null) addToCartBtn.setDisable(!loggedIn);
    }


    // ============================
    // Open details view
    // ============================
    private void openDetails() {
        if (onDetails != null) {
            onDetails.run();
        }
    }

    // ============================
    // Getter
    // ============================
    public FlowerDTO getItem() {
        return item;
    }
}