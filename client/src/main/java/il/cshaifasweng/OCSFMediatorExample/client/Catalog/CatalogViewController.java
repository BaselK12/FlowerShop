package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.ServerMessageEvent;
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

    private final boolean loggedIn = true; // wire this to your session later

    @FXML
    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // kick off initial loads
        try {
            App.getClient().sendToServer(new GetCategoriesRequest());
            App.getClient().sendToServer(new GetPromotionsRequest());
            App.getClient().sendToServer(new GetCatalogRequest(null, null, null, false));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // filters
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        }
        if (categoryCombo != null) {
            categoryCombo.setOnAction(e -> applyFilters());
        }
        if (promoModeCombo != null) {
            promoModeCombo.setOnAction(e -> applyFilters());
        }
    }

    /** Call this when navigating away to avoid EventBus leaks. */
    public void shutdown() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    // ====================== EVENT-BUS Handlers ======================

    @Subscribe
    public void handleCategories(GetCategoriesResponse response) {
        if (response == null || response.getCategories() == null) return;

        Platform.runLater(()->{
            allCategories = response.getCategories();
            if (categoryCombo == null) return;

            categoryCombo.getItems().clear();
            categoryNameToId.clear();

            categoryCombo.getItems().add("All");
            for (CategoryDTO cat : allCategories) {
                String display = cat.getDisplayName() != null ? cat.getDisplayName() : cat.getName();
                categoryCombo.getItems().add(display);
                categoryNameToId.put(display, cat.getId());
            }
            categoryCombo.getSelectionModel().selectFirst();
        });

    }

    @Subscribe
    public void handlePromotions(GetPromotionsResponse response) {
        if (response == null || response.getPromotions() == null) return;

        Platform.runLater(()->{
            allPromotions = response.getPromotions();
            if (promoModeCombo == null) return;

            promoModeCombo.getItems().clear();
            promoModeCombo.getItems().add("All");
            promoModeCombo.getItems().add("Non-Promotions");

            allPromotions.stream()
                    .sorted(Comparator.comparing(PromotionDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .map(PromotionDTO::toString)
                    .forEach(promoModeCombo.getItems()::add);

            promoModeCombo.getSelectionModel().selectFirst();
        });
    }

    @Subscribe
    public void handleCatalog(GetCatalogResponse response) {
        if (response == null || response.getFlowers() == null) return;
        Platform.runLater(()->{
            allFlowers = response.getFlowers();
            renderItems(allFlowers);
        });
    }

    @Subscribe
    public void handleAddToCart(AddToCartResponse response) {
        if (cartCountLabel == null || response == null) return;
        Platform.runLater(()->{
            if (response.isSuccess()) {
                cartCountLabel.setText(response.getCartSize() + " items");
            } else {
                cartCountLabel.setText("Add to cart failed");
            }
        });
    }

    // ====================== Filtering ======================

    private void applyFilters() {
        String search = safeTxt(searchField);
        String selectedCategory = categoryCombo != null ? categoryCombo.getValue() : null;
        String selectedPromo = promoModeCombo != null ? promoModeCombo.getValue() : null;

        Long promotionId = null;
        boolean onlyActivePromotions = false;

        if (selectedPromo != null && !"All".equals(selectedPromo)) {
            if ("Non-Promotions".equals(selectedPromo)) {
                promotionId = 0L;
            } else {
                String clean = selectedPromo.replace(" (Active)", "").replace(" (Expired)", "").trim();
                promotionId = allPromotions.stream()
                        .filter(p -> clean.equalsIgnoreCase(p.getName()))
                        .map(PromotionDTO::getId)
                        .findFirst().orElse(null);
            }
        }

        onlyActivePromotions = selectedPromo != null
                && !"All".equals(selectedPromo)
                && !"Non-Promotions".equals(selectedPromo);

        try {
            GetCatalogRequest req = new GetCatalogRequest(
                    (selectedCategory != null && !"All".equals(selectedCategory)) ? selectedCategory : null,
                    (promotionId != null && promotionId > 0) ? promotionId : null,
                    search.isBlank() ? null : search,
                    onlyActivePromotions
            );
            App.getClient().sendToServer(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String safeTxt(TextField tf) {
        return tf == null || tf.getText() == null ? "" : tf.getText().trim();
    }

    // ====================== Rendering ======================

    private void renderItems(List<FlowerDTO> flowers) {
        if (itemsGrid == null || promotionsStrip == null) return;

        itemsGrid.getChildren().clear();
        promotionsStrip.getChildren().clear();

        if (flowers == null || flowers.isEmpty()) {
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

                if (f.getPromotion() != null && Boolean.TRUE.equals(f.getPromotion().isActive())) {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Catalog/ItemDetails.fxml"));
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
}
