package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.client.ui.ViewTracker;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartState;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
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
import org.greenrobot.eventbus.ThreadMode;

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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Initial UI state based on session
        applySessionToUI();

        // Ask server for the persisted cart
        requestCart();
    }

    /** Ask server for current cart. */
    private void requestCart() {
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
        // Enable checkout only if logged-in and cart not empty
        if (CheckoutBtn != null) {
            boolean loggedIn = ClientSession.getCustomerId() > 0;
            CheckoutBtn.setDisable(!loggedIn || cartItems.isEmpty());
        }
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        TotalLabel.setText("Total: " + currency.format(total));
    }

    private void applySessionToUI() {
        boolean loggedIn = ClientSession.getCustomerId() > 0;
        if (CheckoutBtn != null) {
            CheckoutBtn.setDisable(!loggedIn || cartItems.isEmpty());
        }
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
        // Gate by login
        if (ClientSession.getCustomerId() <= 0) {
            // redirect to login page if unauthenticated
            Nav.go(CheckoutBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml");
            return;
        }
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

    // -------------------- Session + ViewTracker hooks --------------------

    // When login succeeds, enable checkout and pull cart again (server might attach user cart)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        applySessionToUI();
        requestCart();
    }

    // When overview hydrates, also refresh UI/cart
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        applySessionToUI();
        requestCart();
    }

    // When Cart view becomes active and we have no items yet, ask for cart
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActive(ViewTracker.ActiveControllerChanged e) {
        if (e == null) return;
        String id = e.controllerId != null ? e.controllerId : e.getControllerId();
        if (( "Cart".equals(id) || "CartView".equals(id) ) && cartItems.isEmpty()) {
            requestCart();
        }
        // Always re-apply session-driven UI when returning to this view
        applySessionToUI();
    }

    public void dispose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
