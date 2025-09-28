package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Category;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CatalogViewController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> promoModeCombo;
    @FXML private TilePane itemsGrid;
    @FXML private HBox promotionsStrip;
    @FXML private Label cartCountLabel;

    private List<Flower> allFlowers = new ArrayList<>();
    private boolean loggedIn = true; // TODO: replace with actual login state

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);

        // Request the catalog from server
        try {
            SimpleClient.getClient().sendToServer(new GetCatalogRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Search + filters listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryCombo.setOnAction(e -> applyFilters());
        promoModeCombo.setOnAction(e -> applyFilters());
    }

    /**
     * Handles catalog response from server.
     */
    @Subscribe
    public void onGetCatalogResponse(GetCatalogResponse response) {
        Platform.runLater(() -> {
            allFlowers = response.getFlowers();

            // Fill category combo dynamically
            categoryCombo.getItems().clear();
            categoryCombo.getItems().add("All");

            allFlowers.stream()
                    .filter(f -> f.getCategory() != null)
                    .flatMap(f -> f.getCategory().stream())        // flatten list<Category>
                    .filter(Objects::nonNull)
                    .map(Category::toString)                       // convert enum to display text
                    .distinct()
                    .sorted()
                    .forEach(categoryCombo.getItems()::add);

            categoryCombo.getSelectionModel().selectFirst();

            // Fill promo mode combo
            promoModeCombo.getItems().setAll("All", "Promotions Only", "Non-Promotions");
            promoModeCombo.getSelectionModel().selectFirst();

            // Render items initially
            renderItems(allFlowers);
        });
    }

    /**
     * Handles AddToCart responses to update the cart count.
     */
    @Subscribe
    public void onAddToCartResponse(AddToCartResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                cartCountLabel.setText(response.getCartSize() + " items");
            } else {
                System.err.println("AddToCart failed: " + response.getMessage());
                // TODO: show a toast/alert to the user
            }
        });
    }

    /**
     * Apply search and filter logic.
     */
    private void applyFilters() {
        String search = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String category = categoryCombo.getValue();
        String promoFilter = promoModeCombo.getValue();

        List<Flower> filtered = allFlowers.stream()
                // Search in name or descriptions
                .filter(f -> search.isEmpty()
                        || f.getName().toLowerCase().contains(search)
                        || (f.getDescription() != null && f.getDescription().toLowerCase().contains(search))
                        || (f.getShortDescription() != null && f.getShortDescription().toLowerCase().contains(search)))
                // Category filter (matches any category in the list)
                .filter(f -> category == null || category.equals("All")
                        || (f.getCategory() != null &&
                        f.getCategory().stream().anyMatch(c -> c.toString().equals(category))))
                // Promo filter
                .filter(f -> {
                    if (promoFilter == null || promoFilter.equals("All")) return true;
                    if (promoFilter.equals("Promotions Only")) return f.isPromo();
                    if (promoFilter.equals("Non-Promotions")) return !f.isPromo();
                    return true;
                })
                .toList();

        renderItems(filtered);
    }

    /**
     * Renders the item cards into the grid and promotions strip.
     */
    private void renderItems(List<Flower> flowers) {
        itemsGrid.getChildren().clear();
        promotionsStrip.getChildren().clear();

        for (Flower f : flowers) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ItemCard.fxml"));
                StackPane cardNode = loader.load();
                ItemCardController cardController = loader.getController();

                cardController.setData(f, loggedIn, () -> openDetails(f));

                itemsGrid.getChildren().add(cardNode);

                if (f.isPromo()) {
                    FXMLLoader promoLoader = new FXMLLoader(getClass().getResource("ItemCard.fxml"));
                    StackPane promoCardNode = promoLoader.load();
                    ItemCardController promoCardController = promoLoader.getController();
                    promoCardController.setData(f, loggedIn, () -> openDetails(f));

                    promotionsStrip.getChildren().add(promoCardNode);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens the details modal for a flower.
     */
    private void openDetails(Flower flower) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ItemDetails.fxml"));
            Scene scene = new Scene(loader.load());

            ItemDetailsController detailsController = loader.getController();
            detailsController.setItem(flower, loggedIn);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Flower Details");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Optional: call this when leaving the view to avoid EventBus leaks
     */
    public void shutdown() {
        EventBus.getDefault().unregister(this);
    }
}
