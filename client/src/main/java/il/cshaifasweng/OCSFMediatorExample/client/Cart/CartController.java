package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CartController {

    @FXML private Button CheckoutBtn;
    @FXML private Button ContinueBtn;

    @FXML private VBox ItemsBox;

    @FXML private Label TotalLabel;

    private final List<CartItem> cartItems = new ArrayList<>();


    @FXML
    private void initialize() {

        //SimpleClient.getClient().setCartController(this);
        // Example items – later replace with real data
        cartItems.add(new CartItem("SKU1", "Red Roses", "flower",
                "https://i.imgur.com/yYb9xJz.jpg", 2, 10.0));
        cartItems.add(new CartItem("SKU2", "Yellow Tulips", "flower",
                "https://i.imgur.com/X6zq5Xq.jpg", 1, 8.0));

        refreshCart();
    }

    private void refreshCart() {
        ItemsBox.getChildren().clear();
        for (CartItem item : cartItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CartItemCard.fxml"));
                Node card = loader.load();
                CartItemCardController controller = loader.getController();

                controller.setData(item,
                        () -> { cartItems.remove(item); refreshCart(); },
                        this::updateTotal);

                ItemsBox.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
        TotalLabel.setText("Total: ₪" + total);
    }

    @FXML
    private void onContinue(ActionEvent event) {
        try {
            SimpleClient.getClient().sendToServer(new ContinueShoppingRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCheckout(ActionEvent event) {
        try {
            SimpleClient.getClient().sendToServer(new CheckoutRequest(new ArrayList<>(cartItems)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCheckoutResponse(CheckoutResponse response) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout Result");
            alert.setHeaderText(null);
            alert.setContentText(response.getMessage());
            alert.showAndWait();
        });
    }

    public void handleContinueResponse(ContinueShoppingResponse response) {
        Platform.runLater(() -> {
            System.out.println("Continue response: " + response.getMessage());
            // TODO: navigate to catalog scene
        });
    }

    public void handleUpdateResponse(CartUpdateResponse response) {
        Platform.runLater(() -> {
            System.out.println("Server confirmed update for: " + response.getItem().getName());
            // You can sync cartItems list here if needed
        });
    }


}
