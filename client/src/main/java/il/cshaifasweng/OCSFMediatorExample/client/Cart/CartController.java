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

// Catalog + Promotions DTOs (we only read them client-side)
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.PromotionDTO;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Locale;

public class CartController {

    @FXML private VBox ItemsBox;
    @FXML private Label TotalLabel;
    @FXML private Button ContinueBtn;
    @FXML private Button CheckoutBtn;
    private Stage loginStage;

    // Server-cart mirror (raw prices from server)
    private final List<CartItem> cartItems = new ArrayList<>();

    // SKU -> FlowerDTO (includes promotion + effectivePrice)
    private final Map<String, FlowerDTO> flowerBySku = new ConcurrentHashMap<>();
    private volatile boolean catalogRequested = false;

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
        // We need catalog to know which SKUs have active promotions
        requestCatalogIfNeeded();
    }

    private void requestCart() {
        if (ClientSession.getCustomerId() <= 0) return;
        try {
            SimpleClient.getClient().sendToServer(new GetCartRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateTotal(); // optimistic
    }

    private void requestCatalogIfNeeded() {
        if (catalogRequested) return;
        catalogRequested = true;
        try {
            // Pull the whole catalog once. It carries promotion + effectivePrice per SKU.
            SimpleClient.getClient().sendToServer(new GetCatalogRequest(null, null, null, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void render() {
        if (ItemsBox == null) return;
        ItemsBox.getChildren().clear();

        for (CartItem it : cartItems) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/il/cshaifasweng/OCSFMediatorExample/client/Cart/CartItemCard.fxml"));
                Node card = loader.load();
                CartItemCardController ctrl = loader.getController();

                // Clone with effective price (promotion-aware) for display
                CartItem display = cloneWithEffectivePrice(it);

                ctrl.setData(display,
                        // onDelete: optimistic remove (local list + UI), then re-sync
                        () -> {
                            cartItems.removeIf(ci -> ci.getSku().equals(it.getSku()));
                            if (ItemsBox.getChildren().contains(card)) {
                                ItemsBox.getChildren().remove(card);
                            }
                            updateTotal();
                            Platform.runLater(this::requestCart);
                        },
                        // onUpdate: totals should recompute immediately with promos
                        this::updateTotal);

                ItemsBox.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateTotal();
    }

    private CartItem cloneWithEffectivePrice(CartItem src) {
        CartItem dst = new CartItem();
        dst.setSku(src.getSku());
        dst.setName(src.getName());
        dst.setPictureUrl(src.getPictureUrl());
        dst.setQuantity(Math.max(0, src.getQuantity()));

        double promoUnit = effectiveUnitPriceFor(src.getSku(), src.getUnitPrice());
        dst.setUnitPrice(promoUnit);

        return dst;
    }

    /**
     * Decide the unit price the user should SEE:
     * - If we know the FlowerDTO and it has an active promotion, prefer effectivePrice.
     * - If effectivePrice wasn't filled for some reason, derive from PromotionDTO.
     * - Else fall back to the item’s unit price from the server.
     */
    private double effectiveUnitPriceFor(String sku, double fallback) {
        FlowerDTO f = flowerBySku.get(sku);
        if (f == null) return fallback;

        // 1) explicit effective price wins if > 0
        double eff = f.getEffectivePrice();
        if (eff > 0) return eff;

        // 2) compute from embedded PromotionDTO if present and active
        PromotionDTO p = f.getPromotion();
        if (p != null && p.isActive()) {
            String type = p.getType();
            double amount = p.getAmount();
            double base = f.getPrice() > 0 ? f.getPrice() : fallback;

            if ("PERCENT".equalsIgnoreCase(type)) {
                return Math.max(0.0, base * (1.0 - amount / 100.0));
            } else { // FIXED
                return Math.max(0.0, base - amount);
            }
        }

        // 3) otherwise the normal price
        return f.getPrice() > 0 ? f.getPrice() : fallback;
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
        // Promotion-aware total using the effective unit price per SKU
        double total = 0.0;
        for (CartItem it : cartItems) {
            double unit = effectiveUnitPriceFor(it.getSku(), it.getUnitPrice());
            total += unit * Math.max(0, it.getQuantity());
        }

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
            // Ensure we have promos context; if not, go get it and render anyway with fallback.
            requestCatalogIfNeeded();
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
                requestCatalogIfNeeded();
            }
        });
    }

    @Subscribe
    public void onAccount(AccountOverviewResponse res) {
        Platform.runLater(this::applySessionToUI);
    }

    // Catalog arrives here; keep a quick lookup by SKU and refresh the cart view to apply promos
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCatalog(GetCatalogResponse res) {
        if (res == null || res.getFlowers() == null) return;

        for (FlowerDTO f : res.getFlowers()) {
            if (f == null || f.getSku() == null) continue;
            flowerBySku.put(f.getSku(), f);
        }
        // Now we can recompute display prices with promotion applied
        render();
    }

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
            requestCatalogIfNeeded();
        }

        applySessionToUI();
    }

    public void dispose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
