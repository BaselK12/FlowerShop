package il.cshaifasweng.OCSFMediatorExample.client.HomePage;

import il.cshaifasweng.OCSFMediatorExample.client.Catalog.ItemCardController;
import il.cshaifasweng.OCSFMediatorExample.client.Catalog.ItemDetailsController;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.FlowerUpdatedEvent;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.UserLoggedInEvent;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class HomePageController {

    @FXML private StackPane centerStack;   // container for main content + modal overlays
    @FXML private HBox cardsRow1;

    // Header buttons
    @FXML private Button btnHome;
    @FXML private Button btnCatalog;
    @FXML private Button btnCart;
    @FXML private Button btnAccount;
    @FXML private Button btnLogin;
    @FXML private Button btnAdmin;
    @FXML private Button btnShopNow;

    // Details dialog (preloaded)
    private Pane detailsDialogRoot;
    private ItemDetailsController detailsController;

    // Session-ish
    private volatile boolean loggedIn = false;

    // Cache last catalog so we can rebuild cards when login flips
    private List<FlowerDTO> latestFlowers = List.of();

    private static void setVisibleManaged(javafx.scene.Node n, boolean on) {
        if (n != null) {
            n.setVisible(on);
            n.setManaged(on);
        }
    }

    @FXML
    public void initialize() {
        ClientSession.install();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (centerStack == null) {
            throw new IllegalStateException("HomePage.fxml is missing fx:id='centerStack' StackPane.");
        }

        // Initial login state from session
        applySessionFromClient();

        // Ask server for catalog (no special filter)
        try {
            SimpleClient.getClient().sendSafely(new GetCatalogRequest(null, null, null, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Preload details popup into the stack
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemDetails.fxml"));
            detailsDialogRoot = loader.load();
            detailsController = loader.getController();
            detailsDialogRoot.setVisible(false);
            detailsDialogRoot.setManaged(false);
            centerStack.getChildren().add(detailsDialogRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Header navigation
        btnHome.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/HomePage/HomePage.fxml"));
        btnCatalog.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml"));
        btnCart.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Cart/CartView.fxml"));
        btnAccount.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Account/MyAccount.fxml"));
        // Login now opens a modal pop-up (no scene navigation)
        btnLogin.setOnAction(e -> showLoginPopup());
        btnAdmin.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminLoginPage.fxml"));

        // CTA
        btnShopNow.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml"));
    }

    private void applySessionFromClient() {
        loggedIn = ClientSession.getCustomerId() != 0L;

        // Toggle header pieces
        setVisibleManaged(btnAccount, loggedIn);
        setVisibleManaged(btnLogin, !loggedIn);

        // The actual ask: disable Cart when not logged in
        if (btnCart != null) {
            btnCart.setDisable(!loggedIn);
        }
    }

    // =========================
    // Listen: UI login event (kept for compatibility with your existing flow)
    // =========================
    @Subscribe
    public void onUserLoggedIn(UserLoggedInEvent e) {
        Platform.runLater(() -> {
            applySessionFromClient();
            // Rebuild cards so Add-to-Cart becomes enabled without nagging
            rebuildFeaturedCards(latestFlowers);
            // Optional: re-ask server in case user-specific pricing/promos exist
            try {
                SimpleClient.getClient().sendSafely(new GetCatalogRequest(null, null, null, false));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Subscribe
    public void onFlowerUpdated(FlowerUpdatedEvent evt) {
        Platform.runLater(() -> {
            try {
                SimpleClient.getClient().sendSafely(new GetCatalogRequest(null, null, null, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // NEW: react to actual login message so header flips even if no custom event fired
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginResponse(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        applySessionFromClient();
        rebuildFeaturedCards(latestFlowers);
    }

    // NEW: react to overview so display updates after server hydrates the customer
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        applySessionFromClient();
        rebuildFeaturedCards(latestFlowers);
    }

    // =========================
    // Home cards data
    // =========================
    @Subscribe
    public void onCatalogResponse(GetCatalogResponse msg) {
        Platform.runLater(() -> {
            latestFlowers = msg.getFlowers();
            rebuildFeaturedCards(latestFlowers);
        });
    }

    private void rebuildFeaturedCards(List<FlowerDTO> flowers) {
        if (cardsRow1 == null) return;

        cardsRow1.getChildren().clear();

        if (flowers == null || flowers.isEmpty()) {
            Label empty = new Label("No featured flowers yet.");
            empty.getStyleClass().add("muted");
            cardsRow1.getChildren().add(empty);
            return;
        }

        List<FlowerDTO> featured = flowers.stream()
                .sorted(Comparator
                        .comparing(FlowerDTO::hasActivePromotion).reversed()
                        .thenComparing(FlowerDTO::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(6)
                .toList();

        for (FlowerDTO flower : featured) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemCard.fxml"));
                Pane cardRoot = loader.load();
                ItemCardController cardController = loader.getController();

                // Pass current login state; card wires its own add-to-cart
                cardController.setData(flower, loggedIn, () -> openDetails(flower));

                cardsRow1.getChildren().add(cardRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // =========================
    // Login modal
    // =========================
    private void showLoginPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml"));

            javafx.scene.Parent root = loader.load();

            Stage dialog = new Stage(StageStyle.DECORATED);
            dialog.setTitle("Login");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Stage owner = null;
            if (centerStack != null && centerStack.getScene() != null) {
                owner = (Stage) centerStack.getScene().getWindow();
            }
            if (owner != null) dialog.initOwner(owner);

            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Re-evaluate session after the modal closes
        boolean was = loggedIn;
        loggedIn = ClientSession.getCustomerId() != 0L || loggedIn;

        // Centralized header state (this now also disables/enables Cart)
        applySessionFromClient();

        // If we just logged in, refresh featured cards
        if (loggedIn && !was) {
            rebuildFeaturedCards(latestFlowers);
        }
    }


    // =========================
    // Details dialog
    // =========================
//    private void showDetails(FlowerDTO flower) {
//        if (detailsController == null || detailsDialogRoot == null) return;
//        detailsController.setItem(flower, loggedIn);
//        detailsDialogRoot.setVisible(true);
//        detailsDialogRoot.setManaged(true);
//    }
//
//    public void closeDetails() {
//        if (detailsDialogRoot == null) return;
//        detailsDialogRoot.setVisible(false);
//        detailsDialogRoot.setManaged(false);
//    }

    private void openDetails(FlowerDTO flower) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemDetails.fxml"));
            Scene scene = new Scene(loader.load());

            ItemDetailsController detailsController = loader.getController();
            detailsController.setItem(flower, loggedIn);

            Stage stage = new Stage();
            stage.setTitle("Flower Details");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Cart (not used here)
    // =========================
    @SuppressWarnings("unused")
    private void sendAddToCart(FlowerDTO flower) {
        try {
            SimpleClient.getClient().sendToServer(new AddToCartRequest(flower.getSku(), 1));
            System.out.println("Added to cart: " + flower.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Cleanup
    // =========================
    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
