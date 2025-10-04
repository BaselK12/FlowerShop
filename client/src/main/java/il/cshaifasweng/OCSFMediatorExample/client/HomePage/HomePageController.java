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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class HomePageController {

    @FXML private HBox cardsRow1;
    @FXML private Button btnHome, btnCatalog, btnCart, btnAccount, btnLogin, btnAdmin, btnShopNow;
    @FXML private StackPane centerStack; // container for main content + modal overlays

    private Pane detailsDialogRoot;
    private ItemDetailsController detailsController;
    private boolean loggedIn = true; // TODO: replace with actual login/session logic

    @FXML
    public void initialize() {
        // Register event listener
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Request featured flowers from the server
        try {
            SimpleClient.getClient().sendToServer(new GetCatalogRequest(null, null, "featured", false));
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

        // Hook up "Shop Now" button (redirect to catalog)
        btnShopNow.setOnAction(e -> openCatalogPage());
    }

    // ==========================================
    // Server Event: Catalog Data Arrived
    // ==========================================
    @Subscribe
    public void onCatalogResponse(GetCatalogResponse msg) {
        Platform.runLater(() -> {
            List<FlowerDTO> flowers = msg.getFlowers();
            cardsRow1.getChildren().clear();

            for (FlowerDTO flower : flowers) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemCard.fxml"
                    ));
                    Pane cardRoot = loader.load();
                    ItemCardController cardController = loader.getController();

                    // Pass the flower DTO and callbacks
                    cardController.setData(flower, loggedIn, () -> showDetails(flower));

                    // Override Add to Cart action for home
                    // for now
                    //cardController.getAddToCartBtn().setOnAction(e -> sendAddToCart(flower));

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