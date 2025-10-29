package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    @FXML
    private void initialize() {
        // Listen for login/overview so the Add-to-Cart button can enable while the modal is open
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        // Give the button a predictable CSS id for upstream gates (#addToCart)
        if (modalAddToCartBtn != null) {
            modalAddToCartBtn.setId("addToCart");
        }
    }

    /**
     * Populates modal with selected flower data.
     */
    public void setItem(FlowerDTO item, boolean loggedIn) {
        this.item = item;
        this.loggedIn = loggedIn;

        // Title
        if (modalTitle != null) {
            modalTitle.setText(item.getName() != null ? item.getName() : "");
        }

        // Description
        String desc = (item.getShortDescription() != null && !item.getShortDescription().isEmpty())
                ? item.getShortDescription()
                : "No description available.";
        if (modalDescription != null) {
            modalDescription.setText(desc);
        }

        // Categories
        List<String> categories = item.getCategories();
        if (modalCategoryChip != null) {
            if (categories != null && !categories.isEmpty()) {
                modalCategoryChip.setText(String.join(", ", categories));
            } else {
                modalCategoryChip.setText("Uncategorized");
            }
        }

        // Price and promotion (null-safe)
        double price = item.getPrice();
        double effectivePrice = item.getEffectivePrice();
        boolean hasActivePromo = item.getPromotion() != null && Boolean.TRUE.equals(item.getPromotion().isActive());

        if (modalPrice != null) {
            if (hasActivePromo && effectivePrice < price) {
                modalPrice.setText(String.format("Price: $%.2f  (Now: $%.2f)", price, effectivePrice));
            } else {
                modalPrice.setText(String.format("Price: $%.2f", price));
            }
        }

        // Image (defensive)
        if (modalImage != null) {
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
        }

        // Disable AddToCart if not logged in
        if (modalAddToCartBtn != null) {
            modalAddToCartBtn.setDisable(!loggedIn);
            modalAddToCartBtn.setOnAction(e -> sendAddToCart());
        }
        if (modalCloseBtn != null) {
            modalCloseBtn.setOnAction(e -> closeWindow());
        }
    }

    private void loadPlaceholder() {
        if (modalImage == null) return;
        try {
            var url = getClass().getResource("/images/placeholder.png");
            if (url != null) {
                modalImage.setImage(new Image(url.toExternalForm()));
            } else {
                modalImage.setImage(null);
            }
        } catch (Exception ignored) {
            modalImage.setImage(null);
        }
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
        // Unregister to avoid leaks if modal is reused
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        try {
            Stage stage = (Stage) (modalCloseBtn != null ? modalCloseBtn.getScene().getWindow() : null);
            if (stage != null) stage.close();
        } catch (Exception ignored) { }
    }

    /** Optional: allow parent to toggle login state without reloading the modal. */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        if (modalAddToCartBtn != null) modalAddToCartBtn.setDisable(!loggedIn);
    }

    // ---- Session hooks: enable Add-to-Cart if login completes while this modal is open ----

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        setLoggedIn(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        setLoggedIn(true);
    }
}
