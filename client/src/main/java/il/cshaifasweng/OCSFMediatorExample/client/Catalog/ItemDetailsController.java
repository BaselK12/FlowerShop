package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ItemDetailsController {

    @FXML private Label modalTitle;
    @FXML private ImageView modalImage;
    @FXML private Label modalCategoryChip;
    @FXML private Label modalPrice;
    @FXML private Label modalDescription;
    @FXML private Button modalCloseBtn;
    @FXML private Button modalAddToCartBtn;

    private Flower item;
    private boolean loggedIn;

    /**
     * Called by CatalogController when opening the details popup.
     */
    public void setItem(Flower item, boolean loggedIn) {
        this.item = item;
        this.loggedIn = loggedIn;

        // Fill UI with flower data
        modalTitle.setText(item.getName());
        modalPrice.setText("$" + item.getPrice());
        modalDescription.setText(item.getDescription());

        // Show category list
        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            modalCategoryChip.setText(
                    item.getCategory().stream()
                            .map(Enum::toString) // or Category::getDisplayName
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("Uncategorized")
            );
        } else {
            modalCategoryChip.setText("Uncategorized");
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                modalImage.setImage(new Image(item.getImageUrl(), true));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + item.getImageUrl());
                // fallback image
                modalImage.setImage(new Image(getClass().getResource("/images/placeholder.png").toExternalForm()));
            }
        }

        // Disable Add to Cart if user is not logged in
        modalAddToCartBtn.setDisable(!loggedIn);

        // Wire button actions
        modalAddToCartBtn.setOnAction(e -> sendAddToCart());
        modalCloseBtn.setOnAction(e -> closeWindow());
    }

    /**
     * Sends an AddToCartRequest to the server.
     */
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

    /**
     * Closes the details modal window.
     */
    private void closeWindow() {
        Stage stage = (Stage) modalCloseBtn.getScene().getWindow();
        Platform.runLater(stage::close);
    }
}
