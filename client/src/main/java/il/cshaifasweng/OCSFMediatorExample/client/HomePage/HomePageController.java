package il.cshaifasweng.OCSFMediatorExample.client.HomePage;

import il.cshaifasweng.OCSFMediatorExample.client.Catalog.ItemCardController;
import il.cshaifasweng.OCSFMediatorExample.client.Catalog.ItemDetailsController;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;


import java.io.IOException;
import java.util.List;

public class HomePageController {

    @FXML private StackPane centerStack;
    @FXML private HBox cardsRow1;
    @FXML private Button btnHome;
    @FXML private Button btnCatalog;
    @FXML private Button btnCart;
    @FXML private Button btnAccount;
    @FXML private Button btnLogin;
    @FXML private Button btnAdmin;
    @FXML private Button btnShopNow;
; // container for main content + modal overlays

    private Pane detailsDialogRoot;
    private ItemDetailsController detailsController;
    private boolean loggedIn = true; // TODO: replace with actual login/session logic

    private static void setVisibleManaged(javafx.scene.Node n, boolean on) {
        n.setVisible(on);
        n.setManaged(on);
    }

    @FXML
    public void initialize() {
        // Session sidecar for login state
        ClientSession.install();

        // EventBus subscription
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (centerStack == null) {
            throw new IllegalStateException("HomePage.fxml is missing fx:id='centerStack' StackPane.");
        }

        // Ask server for catalog (no bogus "featured" filter)
        try {
            SimpleClient.getClient().sendToServer(new GetCatalogRequest(null, null, null, false));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Preload details popup
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemDetails.fxml"
            ));
            detailsDialogRoot = loader.load();
            detailsController = loader.getController();
            detailsDialogRoot.setVisible(false);
            detailsDialogRoot.setManaged(false);
            centerStack.getChildren().add(detailsDialogRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Header buttons: wire navigation
        btnHome.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/HomePage/HomePage.fxml"));
        btnCatalog.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml"));
        btnCart.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Cart/CartView.fxml"));
        btnAccount.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Account/MyAccount.fxml"));
        btnLogin.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml"));
        btnAdmin.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminLoginPage.fxml"));

        // CTA "Shop Now" -> Catalog
        btnShopNow.setOnAction(e ->
                Nav.go(centerStack, "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml"));

        // Toggle Login vs My Account
        boolean loggedIn = ClientSession.getCustomerId() != 0L;
        setVisibleManaged(btnAccount, loggedIn);
        setVisibleManaged(btnLogin, !loggedIn);
    }


    // ==========================================
    // Server Event: Catalog Data Arrived
    // ==========================================
    @Subscribe
    public void onCatalogResponse(GetCatalogResponse msg) {
        Platform.runLater(() -> {
            List<FlowerDTO> flowers = msg.getFlowers();
            cardsRow1.getChildren().clear();

            // Sort: promos first, then by name, cap to 6
            List<FlowerDTO> featured = flowers.stream()
                    .sorted(Comparator
                            .comparing(FlowerDTO::hasActivePromotion).reversed()
                            .thenComparing(FlowerDTO::getName, String.CASE_INSENSITIVE_ORDER))
                    .limit(6)
                    .toList();

            if (featured.isEmpty()) {
                // graceful empty state
                Label empty = new Label("No featured flowers yet.");
                empty.getStyleClass().add("muted");
                cardsRow1.getChildren().add(empty);
                return;
            }

            for (FlowerDTO flower : featured) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemCard.fxml"
                    ));
                    Pane cardRoot = loader.load();
                    ItemCardController cardController = loader.getController();

                    // card handles its own Add to Cart; we supply onDetails callback
                    cardController.setData(flower, ClientSession.getCustomerId() != 0L, () -> showDetails(flower));

                    cardsRow1.getChildren().add(cardRoot);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // ==========================================
    // Cart + Details actions
    // ==========================================

    private void sendAddToCart(FlowerDTO flower) {
        try {
            SimpleClient.getClient().sendToServer(new AddToCartRequest(flower.getSku(), 1));
            System.out.println("Added to cart: " + flower.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(FlowerDTO flower) {
        detailsController.setItem(flower, loggedIn);
        detailsDialogRoot.setVisible(true);
        detailsDialogRoot.setManaged(true);
    }

    public void closeDetails() {
        detailsDialogRoot.setVisible(false);
        detailsDialogRoot.setManaged(false);
    }

    // ==========================================
    // Navigation
    // ==========================================

    private void openCatalogPage() {
        // TODO: implement navigation to catalog scene if applicable
        System.out.println("Navigate to catalog...");
    }

    // ==========================================
    // Cleanup
    // ==========================================
    public void onClose() {
        EventBus.getDefault().unregister(this);
    }
}