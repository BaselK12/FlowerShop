package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.client.ui.ViewTracker;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.*;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartState;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartController {

    @FXML private VBox ItemsBox;
    @FXML private Label TotalLabel;
    @FXML private Button ContinueBtn;
    @FXML private Button CheckoutBtn;
    private Stage loginStage;

    // Local mirror
    private final List<CartItem> cartItems = new ArrayList<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    private void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        applySessionToUI();

        if (ClientSession.getCustomerId() <= 0) {
            Platform.runLater(() -> openLoginModal(ItemsBox != null ? ItemsBox : CheckoutBtn));
            return;
        }

        requestCart();
    }

    private void requestCart() {
        if (ClientSession.getCustomerId() <= 0) return;
        try {
            SimpleClient.getClient().sendToServer(new GetCartRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateTotal();
    }

    private void render() {
        if (ItemsBox == null) return;
        ItemsBox.getChildren().clear();

        for (CartItem it : cartItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Cart/CartItemCard.fxml"));
                Node card = loader.load();
                CartItemCardController ctrl = loader.getController();

                ctrl.setData(it,
                        // onDelete: optimistic remove (local list + UI), then re-sync
                        () -> {
                            cartItems.removeIf(ci -> ci.getSku().equals(it.getSku()));
                            if (ItemsBox.getChildren().contains(card)) {
                                ItemsBox.getChildren().remove(card);
                            }
                            updateTotal();
                            Platform.runLater(this::requestCart);
                        },
                        // onUpdate: just recompute totals immediately
                        this::updateTotal);

                ItemsBox.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateTotal();
    }

    private void openLoginModal(Node owner) {
        if (loginStage != null && loginStage.isShowing()) {
            loginStage.requestFocus();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml"));
            Parent root = loader.load();

            loginStage = new Stage(StageStyle.DECORATED);
            loginStage.setTitle("Sign in");
            loginStage.initModality(Modality.WINDOW_MODAL);

            if (owner != null && owner.getScene() != null && owner.getScene().getWindow() != null) {
                loginStage.initOwner(owner.getScene().getWindow());
            }

            loginStage.setScene(new javafx.scene.Scene(root));
            loginStage.centerOnScreen();
            loginStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        if (TotalLabel != null) TotalLabel.setText("Total: " + currency.format(total));
        if (CheckoutBtn != null) {
            boolean loggedIn = ClientSession.getCustomerId() > 0;
            CheckoutBtn.setDisable(!loggedIn || cartItems.isEmpty());
        }
    }

    private void applySessionToUI() {
        boolean loggedIn = ClientSession.getCustomerId() > 0;
        if (CheckoutBtn != null) CheckoutBtn.setDisable(!loggedIn || cartItems.isEmpty());
    }

    @FXML
    private void onContinue() {
        Nav.go(ContinueBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml");
    }

    @FXML
    private void onCheckout() {
        if (ClientSession.getCustomerId() <= 0) {
            openLoginModal(CheckoutBtn);
        } else {
            Nav.go(CheckoutBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Checkout.fxml");
        }
    }

    // ---------- EventBus listeners ----------

    @Subscribe
    public void onCartState(CartState s) {
        Platform.runLater(() -> {
            cartItems.clear();
            if (s.getItems() != null) cartItems.addAll(s.getItems());
            render();
        });
    }

    @Subscribe
    public void onCartUpdate(CartUpdateResponse res) {
        Platform.runLater(this::requestCart);
    }

    @Subscribe
    public void onRemove(RemoveFromCartResponse res) {
        Platform.runLater(this::requestCart);
    }

    @Subscribe
    public void onCheckoutResponse(CheckoutResponse res) {
        Platform.runLater(this::requestCart);
    }

    @Subscribe
    public void onLogin(LoginResponse res) {
        Platform.runLater(() -> {
            applySessionToUI();
            if (res != null && res.isOk()) {
                if (loginStage != null && loginStage.isShowing()) {
                    loginStage.close();
                }
                requestCart();
            }
        });
    }

    @Subscribe
    public void onAccount(AccountOverviewResponse res) { Platform.runLater(this::applySessionToUI); }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActive(ViewTracker.ActiveControllerChanged e) {
        if (e == null) return;
        String id = e.controllerId != null ? e.controllerId : e.getControllerId();

        if ("Cart".equals(id) || "CartView".equals(id)) {
            if (ClientSession.getCustomerId() <= 0) {
                openLoginModal(ItemsBox != null ? ItemsBox : CheckoutBtn);
                return;
            }
            if (cartItems.isEmpty()) {
                requestCart();
            }
        }

        applySessionToUI();
    }

    public void dispose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
