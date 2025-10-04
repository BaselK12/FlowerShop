package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Category;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Flower;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.*;
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
import java.util.*;
import java.util.stream.Collectors;

public class CatalogViewController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> promoModeCombo;
    @FXML private TilePane itemsGrid;
    @FXML private HBox promotionsStrip;
    @FXML private Label cartCountLabel;

    private List<FlowerDTO> allFlowers = new ArrayList<>();
    private List<CategoryDTO> allCategories = new ArrayList<>();
    private List<PromotionDTO> allPromotions = new ArrayList<>();
    private final Map<String, Long> categoryNameToId = new HashMap<>();

    private boolean loggedIn = true; // TODO: connect to actual login session

    @FXML
    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Request initial data
        try {
            App.getClient().sendToServer(new GetCategoriesRequest());
            App.getClient().sendToServer(new GetPromotionsRequest());
            App.getClient().sendToServer(new GetCatalogRequest(null, null, null, false));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        categoryCombo.setOnAction(e -> applyFilters());
        promoModeCombo.setOnAction(e -> applyFilters());
    }

    // =======================================
    // EventBus Handlers (Server Responses)
    // =======================================

    @Subscribe
    public void onGetCatalogResponse(GetCatalogResponse response) {
        Platform.runLater(() -> {
            allFlowers = response.getFlowers();
            renderItems(allFlowers);
        });
    }

    @Subscribe
    public void onGetCategoriesResponse(GetCategoriesResponse response) {
        Platform.runLater(() -> {
            allCategories = response.getCategories();
            categoryCombo.getItems().clear();
            categoryCombo.getItems().add("All");
            categoryNameToId.clear();

            for (CategoryDTO cat : allCategories) {
                String display = cat.getDisplayName() != null ? cat.getDisplayName() : cat.getName();
                categoryCombo.getItems().add(display);
                categoryNameToId.put(display, cat.getId());
            }

            categoryCombo.getSelectionModel().selectFirst();
        });
    }

    @Subscribe
    public void onGetPromotionsResponse(GetPromotionsResponse response) {
        Platform.runLater(() -> {
            allPromotions = response.getPromotions();
            promoModeCombo.getItems().clear();
            promoModeCombo.getItems().add("All");
            promoModeCombo.getItems().add("Non-Promotions");

            // Add all promotions (active + expired) for visibility
            allPromotions.stream()
                    .sorted(Comparator.comparing(PromotionDTO::getName))
                    .map(PromotionDTO::toString)
                    .forEach(promoModeCombo.getItems()::add);

            promoModeCombo.getSelectionModel().selectFirst();
        });
    }

    @Subscribe
    public void onAddToCartResponse(AddToCartResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                cartCountLabel.setText(response.getCartSize() + " items");
            } else {
                System.err.println("AddToCart failed: " + response.getMessage());
            }
        });
    }

    // =======================================
    // Apply Filters → Server Request
    // =======================================

    private void applyFilters() {
        String search = searchField.getText() != null ? searchField.getText().trim() : "";
        String selectedCategory = categoryCombo.getValue();
        String selectedPromo = promoModeCombo.getValue();

        Long categoryId = null;
        Long promotionId = null;
        boolean onlyActivePromotions = false;

        // Category filter
        if (selectedCategory != null && !selectedCategory.equals("All")) {
            categoryId = categoryNameToId.get(selectedCategory);
        }

        // Promotion filter
        if (selectedPromo != null && !selectedPromo.equals("All")) {
            if (selectedPromo.equals("Non-Promotions")) {
                promotionId = 0L;
            } else {
                // Remove suffixes like " (Active)" / " (Expired)"
                String cleanPromoName = selectedPromo.replace(" (Active)", "").replace(" (Expired)", "").trim();

                promotionId = allPromotions.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(cleanPromoName))
                        .map(PromotionDTO::getId)
                        .findFirst()
                        .orElse(null);
            }
        }

        // Whether user asked for active promos only
        onlyActivePromotions = selectedPromo != null
                && !selectedPromo.equals("All")
                && !selectedPromo.equals("Non-Promotions");

        // Send request
        try {
            GetCatalogRequest req = new GetCatalogRequest(
                    categoryId != null ? categoryId.toString() : null,
                    (promotionId != null && promotionId > 0) ? promotionId : null,
                    search.isEmpty() ? null : search,
                    onlyActivePromotions
            );
            App.getClient().sendToServer(req);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =======================================
    // Rendering UI
    // =======================================

    private void renderItems(List<FlowerDTO> flowers) {
        itemsGrid.getChildren().clear();
        promotionsStrip.getChildren().clear();

        if (flowers.isEmpty()) {
            Label empty = new Label("No flowers found.");
            empty.getStyleClass().add("empty-message");
            itemsGrid.getChildren().add(empty);
            return;
        }

        for (FlowerDTO f : flowers) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ItemCard.fxml"));
                StackPane cardNode = loader.load();
                ItemCardController cardController = loader.getController();
                cardController.setData(f, loggedIn, () -> openDetails(f));

                itemsGrid.getChildren().add(cardNode);

                if (f.getPromotion() != null && f.getPromotion().isActive()) {
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

    private void openDetails(FlowerDTO flower) {
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

    public void shutdown() {
        EventBus.getDefault().unregister(this);
    }
}
