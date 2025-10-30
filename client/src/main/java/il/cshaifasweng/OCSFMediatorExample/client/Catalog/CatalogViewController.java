package il.cshaifasweng.OCSFMediatorExample.client.Catalog;

import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.UserLoggedInEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.*;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import org.greenrobot.eventbus.ThreadMode;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CatalogViewController {

    // Top bar
    @FXML private Button BackBtn;
    @FXML private Button LoginBtn;
    @FXML private Node Root;

    // Filters
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> promoModeCombo;

    @FXML private Button BouquetBtn;
    @FXML private Button cartBtn;


    // Content
    @FXML private TilePane itemsGrid;
    @FXML private HBox promotionsStrip;
    @FXML private ScrollPane itemsScroll;
    @FXML private ScrollPane promotionsScroll;

    // Cart pill
    @FXML private Label cartCountLabel;

    // Data
    private List<FlowerDTO> allFlowers = new ArrayList<>();
    private List<CategoryDTO> allCategories = new ArrayList<>();
    private List<PromotionDTO> allPromotions = new ArrayList<>();
    private final Map<String, Long> categoryNameToId = new HashMap<>();

    // Session-ish state
    private volatile boolean loggedIn = false;
    private String loggedInUsername = null;
    private String loggedInDisplayName = null;

    // ====================== Life-cycle ======================

    @FXML
    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Initial login state probe (fallback; primary is the event)
        loggedIn = (ClientSession.getCustomerId() > 0) || detectLoggedInSafely();
        if (LoginBtn != null) {
            LoginBtn.setVisible(!loggedIn);
            LoginBtn.setManaged(!loggedIn);
        }

        if (itemsScroll != null) {
            itemsScroll.setFitToWidth(true);
            itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            itemsScroll.setPannable(true);
        }
        if (promotionsScroll != null) {
            promotionsScroll.setFitToHeight(true);
        }

        // Kick off initial loads
        try {
            App.getClient().sendToServer(new GetCategoriesRequest());
            App.getClient().sendToServer(new GetPromotionsRequest());
            App.getClient().sendToServer(new GetCatalogRequest(null, null, null, false));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Filters wiring
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        }
        if (categoryCombo != null) {
            categoryCombo.setOnAction(e -> applyFilters());
        }
        if (promoModeCombo != null) {
            promoModeCombo.setOnAction(e -> applyFilters());
        }

        cartBtn.setOnAction(e ->
                Nav.go(cartBtn, "/il/cshaifasweng/OCSFMediatorExample/client/Cart/CartView.fxml"));
        BouquetBtn.setOnAction(e ->
                Nav.go(BouquetBtn, "/il/cshaifasweng/OCSFMediatorExample/client/CreateBouquet.fxml"));
    }

    /** Call this when navigating away to avoid EventBus leaks. */
    public void shutdown() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    // ====================== UI Actions ======================

    @FXML
    private void onBack() {
        Nav.go(Root, "/il/cshaifasweng/OCSFMediatorExample/client/HomePage/HomePage.fxml");
    }

    @FXML
    private void onLogin() {
        showLoginPopup();
    }

    // ====================== Login event hook ======================

    /** Primary path: react instantly when login succeeds. */
    @Subscribe
    public void onUserLoggedIn(UserLoggedInEvent e) {
        Platform.runLater(() -> {
            loggedIn = true;
            loggedInUsername = e.username();
            loggedInDisplayName = e.displayName();

            if (LoginBtn != null) {
                LoginBtn.setVisible(false);
                LoginBtn.setManaged(false);
            }

            // Light refresh so cards know they can add-to-cart without nagging
            renderItems(allFlowers);

            // Optional: pull fresh catalog with same filters so server-side
            // promotion/pricing rules that depend on user kick in.
            applyFilters();
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;

        loggedIn = true;
        // If you want to show name: loggedInDisplayName = (loggedInDisplayName == null ? "" : loggedInDisplayName);
        if (LoginBtn != null) {
            LoginBtn.setVisible(false);
            LoginBtn.setManaged(false);
        }
        // Re-render so cards enable Add-to-Cart without nagging
        renderItems(allFlowers);
        // And pull fresh catalog with current filters (user-specific promos/pricing)
        applyFilters();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;

        loggedIn = true;
        // Optionally capture display name
        try { loggedInDisplayName = r.getCustomer().getDisplayName(); } catch (Exception ignored) {}

        if (LoginBtn != null) {
            LoginBtn.setVisible(false);
            LoginBtn.setManaged(false);
        }
        renderItems(allFlowers);
        applyFilters();
    }


    // ====================== Helpers: Login popup & detection ======================

    /** Opens the login window as a modal dialog. */
    private void showLoginPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml")
            );
            Parent view = loader.load();

            Stage dialog = new Stage(StageStyle.DECORATED);
            dialog.setTitle("Login");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Stage owner = null;
            if (Root != null && Root.getScene() != null) {
                owner = (Stage) Root.getScene().getWindow();
            }
            if (owner != null) dialog.initOwner(owner);

            dialog.setScene(new Scene(view));
            dialog.setResizable(false);
            dialog.showAndWait();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Fallback: if for any reason we missed the event, re-detect.
        boolean wasLoggedIn = loggedIn;
        loggedIn = (ClientSession.getCustomerId() > 0) || detectLoggedInSafely() || loggedIn;
        if (loggedIn && !wasLoggedIn) {
            if (LoginBtn != null) {
                LoginBtn.setVisible(false);
                LoginBtn.setManaged(false);
            }
            renderItems(allFlowers);
            applyFilters();
        }
    }

    /**
     * Defensive, reflection-based login detection to avoid tight coupling.
     */
    private boolean detectLoggedInSafely() {
        try {
            Object session = null;
            for (String m : List.of("get", "current", "instance")) {
                try {
                    Method mm = ClientSession.class.getMethod(m);
                    session = mm.invoke(null);
                    if (session != null) break;
                } catch (NoSuchMethodException ignored) { }
            }
            if (session == null) return false;

            for (String m : List.of("isLoggedIn", "isAuthenticated", "hasUser", "hasAccount")) {
                try {
                    Method mm = session.getClass().getMethod(m);
                    Object val = mm.invoke(session);
                    if (val instanceof Boolean b) return b;
                } catch (NoSuchMethodException ignored) { }
            }

            for (String m : List.of("getAccount", "getCustomer", "getUser", "getPrincipal")) {
                try {
                    Method mm = session.getClass().getMethod(m);
                    Object obj = mm.invoke(session);
                    if (obj != null) return true;
                } catch (NoSuchMethodException ignored) { }
            }
        } catch (Exception ignored) { }
        return false;
    }

    // ====================== Event-Bus Handlers (server msgs) ======================

    @Subscribe
    public void handleCategories(GetCategoriesResponse response) {
        if (response == null || response.getCategories() == null) return;

        Platform.runLater(() -> {
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

        Platform.runLater(() -> {
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
        Platform.runLater(() -> {
            allFlowers = response.getFlowers();
            renderItems(allFlowers);
        });
    }

    @Subscribe
    public void handleAddToCart(AddToCartResponse response) {
        if (cartCountLabel == null || response == null) return;
        Platform.runLater(() -> {
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

                // Pass current login state to card so it can style/disable appropriately
                cardController.setData(f, loggedIn, () -> openDetails(f));

                // Guard Add-to-Cart when not logged in
                wireLoginGateToCard(cardNode);

                itemsGrid.getChildren().add(cardNode);

                if (f.getPromotion() != null && Boolean.TRUE.equals(f.getPromotion().isActive())) {
                    FXMLLoader promoLoader = new FXMLLoader(getClass().getResource("ItemCard.fxml"));
                    StackPane promoCardNode = promoLoader.load();
                    ItemCardController promoCardController = promoLoader.getController();
                    promoCardController.setData(f, loggedIn, () -> openDetails(f));
                    wireLoginGateToCard(promoCardNode);
                    promotionsStrip.getChildren().add(promoCardNode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** If a known "Add to Cart" Button exists in the card, intercept click when not logged in. */
    private void wireLoginGateToCard(StackPane cardNode) {
        if (cardNode == null) return;
        List<String> candidates = List.of("#AddToCartBtn", "#AddBtn", "#addToCart", "#addBtn");
        for (String sel : candidates) {
            Node n = cardNode.lookup(sel);
            if (n instanceof Button b) {
                b.addEventFilter(ActionEvent.ACTION, ev -> {
                    if (!loggedIn) {
                        ev.consume();
                        showLoginPopup();
                    }
                });
                break;
            }
        }
    }

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
}
