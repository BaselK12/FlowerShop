package il.cshaifasweng.OCSFMediatorExample.client.HomePage;

import il.cshaifasweng.OCSFMediatorExample.client.Catalog.ItemCardController;
import il.cshaifasweng.OCSFMediatorExample.client.Catalog.ItemDetailsController;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogResponse;
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

    @FXML private StackPane centerStack; // wrap hero+featured VBox in FXML to overlay details

    private Pane detailsDialogRoot;
    private ItemDetailsController detailsController;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);

        // Ask the server for featured flowers
        try {
            SimpleClient.getClient().sendToServer(new GetCatalogRequest("featured"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Preload details dialog (hidden initially)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemDetails.fxml"));
            detailsDialogRoot = loader.load();
            detailsController = loader.getController();
            detailsDialogRoot.setVisible(false);
            detailsDialogRoot.setManaged(false);

            centerStack.getChildren().add(detailsDialogRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // When catalog response arrives from server
    @Subscribe
    public void onCatalogResponse(GetCatalogResponse msg) {
        List<Flower> flowers = msg.getFlowers();
        cardsRow1.getChildren().clear();

        for (Flower flower : flowers) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemCard.fxml"));
                Pane cardRoot = loader.load();

                ItemCardController cardController = loader.getController();
                // Pass the flower + callbacks
                cardController.setItem(flower,
                        () -> sendAddToCart(flower),
                        () -> showDetails(flower));

                cardsRow1.getChildren().add(cardRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAddToCart(Flower flower) {
        try {
            SimpleClient.getClient().sendToServer(new AddToCartRequest(flower.getSku(), 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(Flower flower) {
        detailsController.setItem(flower, true);
        detailsDialogRoot.setVisible(true);
        detailsDialogRoot.setManaged(true);
    }

    public void closeDetails() {
        detailsDialogRoot.setVisible(false);
        detailsDialogRoot.setManaged(false);
    }

    public void onClose() {
        EventBus.getDefault().unregister(this);
    }
}
