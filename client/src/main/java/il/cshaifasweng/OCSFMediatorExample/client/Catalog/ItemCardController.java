package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;

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

    private Flower item;
    private Runnable onDetails;
    private boolean loggedIn;

    public void setData(Flower item, boolean loggedIn, Runnable onDetails) {
        this.item = item;
        this.onDetails = onDetails;
        this.loggedIn = loggedIn;

        // Name
        nameLabel.setText(item.getName());

        // Category display
        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            categoryChip.setText(
                    item.getCategory().stream()
                            .map(Enum::toString) // Or .map(Category::getDisplayName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("Uncategorized")
            );
        } else {
            categoryChip.setText("Uncategorized");
        }

        // Short description
        if (item.getShortDescription() != null && !item.getShortDescription().isEmpty()) {
            shortDescLabel.setText(item.getShortDescription());
        } else {
            shortDescLabel.setText(item.getDescription());
        }

        // Price
        priceLabel.setText("$" + item.getPrice());

        // Image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                productImage.setImage(new Image(item.getImageUrl(), true));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + item.getImageUrl());
            }
        }

        // Promo flag
        promoRibbon.setVisible(item.isPromo());

        // Login state
        addToCartBtn.setDisable(!loggedIn);

        // Events
        addToCartBtn.setOnAction(e -> sendAddToCart());
        detailsBtn.setOnAction(e -> openDetails());
        itemCardRoot.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openDetails();
            }
        });
    }

    private void sendAddToCart() {
        if (item == null) return;

        try {
            SimpleClient.getClient().sendToServer(
                    new AddToCartRequest(item.getSku(), 1)
            );
            System.out.println("Sent AddToCartRequest for sku=" + item.getSku());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDetails() {
        if (onDetails != null) {
            onDetails.run();
        }
    }
}