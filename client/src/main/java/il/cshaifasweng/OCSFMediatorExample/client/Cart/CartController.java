package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartController {

    @FXML private Button CheckoutBtn;
    @FXML private Button ContinueBtn;
    @FXML private VBox ItemsBox;
    @FXML private Label TotalLabel;

    // Local mirror of the cart to render the view.
    private final List<CartItem> cartItems = new ArrayList<>();

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("he", "IL"));

    @FXML
    private void initialize() {
        // Listen for server events
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Ask server for the persisted cart instead of faking items
        try {
            SimpleClient.getClient().sendToServer(new GetCartRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Rebuilds the VBox of item cards and updates the total label. */
    private void refreshCart() {
        ItemsBox.getChildren().clear();

        for (CartItem item : cartItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CartItemCard.fxml"));
                Node card = loader.load();
                CartItemCardController controller = loader.getController();

                controller.setData(
                        item,
                        // onDelete
                        () -> {
                            cartItems.removeIf(ci -> ci.getSku().equals(item.getSku()));
                            refreshCart();
                        },
                        // onUpdate
                        this::updateTotal
                );

                ItemsBox.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        TotalLabel.setText("Total: " + currency.format(total));
    }

    // -------------------- UI actions --------------------

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
            // Your DTO currently expects items; if you later switch to an empty request, this stays harmless.
            SimpleClient.getClient().sendToServer(new CheckoutRequest(new ArrayList<>(cartItems)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------- EventBus handlers from server --------------------

    @Subscribe
    public void onCartState(CartState state) {
        Platform.runLater(() -> {
            cartItems.clear();
            cartItems.addAll(state.getItems());
            refreshCart();
        });
    }

    @Subscribe
    public void onCheckoutResponse(CheckoutResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                cartItems.clear();
                refreshCart();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout");
            alert.setHeaderText(null);
            alert.setContentText(response.getMessage());
            alert.showAndWait();
        });
    }

    @Subscribe
    public void onContinueResponse(ContinueShoppingResponse response) {
        Platform.runLater(() -> {
            System.out.println("Continue response: " + response.getMessage());
            Nav.go(ContinueBtn,"/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml");
        });
    }

    @Subscribe
    public void onCartUpdateResponse(CartUpdateResponse response) {
        Platform.runLater(() -> {
            CartItem updated = response.getItem();
            // Sync local list with the server-confirmed item
            cartItems.removeIf(ci -> ci.getSku().equals(updated.getSku()));
            if (updated.getQuantity() > 0) {
                cartItems.add(updated);
            }
            refreshCart(); // rebuild cards and total
        });
    }


    public void dispose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
