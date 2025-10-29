package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.client.common.Session;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyAccountController {

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton ProfileBtn;
    @FXML private ToggleButton CouponsBtn;
    @FXML private ToggleButton OrdersBtn;
    @FXML private ToggleButton PaymentsBtn;
    @FXML private ToggleButton ComplaintsBtn;   // << NEW

    @FXML private Button LogOutBtn;
    @FXML private Button CloseBtn;

    @FXML private StackPane ContentStack;

    // Cache loaded FXMLs so switching tabs doesn’t re-parse files every time
    private final Map<String, Node> cache = new HashMap<>();

    @FXML
    private void initialize() {
        ClientSession.install();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // If somebody opened "My Account" while logged out, take them to Login
        if (ClientSession.getCustomerId() <= 0) {
            Nav.go(ContentStack, "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml");
            return;
        }

        // Keep UI state and loaded view in sync
        ProfileBtn.setSelected(true);
        loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/ProfileView.fxml");

        // Navigation
        navGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            if (newToggle == ProfileBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/ProfileView.fxml");
            } else if (newToggle == CouponsBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/CouponsView.fxml");
            } else if (newToggle == OrdersBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/PastOrdersView.fxml");
            } else if (newToggle == PaymentsBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/PaymentsView.fxml");
            } else if (newToggle == ComplaintsBtn) { // << NEW
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/CustomerComplaintsView.fxml");
            }
        });

        // Buttons
        LogOutBtn.setOnAction(e -> handleLogout());
        CloseBtn.setOnAction(e -> handleClose());
    }

    private void loadView(String fxmlPath) {
        try {
            Node view = cache.get(fxmlPath);
            if (view == null) {
                var url = getClass().getResource(fxmlPath);
                if (url == null) {
                    throw new IllegalArgumentException("FXML not found on classpath: " + fxmlPath);
                }
                FXMLLoader loader = new FXMLLoader(url);
                view = loader.load();

                Object c = loader.getController();
                if (c instanceof RequiresSession rs) {
                    long id = ClientSession.getCustomerId();  // use the cached id
                    rs.setCustomerId(id);
                }

                // << NEW: stash controller so we can re-inject customerId later without reloading
                view.getProperties().put("__controller", c);

                cache.put(fxmlPath, view);
            }
            ContentStack.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load: " + fxmlPath, e);
        }
    }

    private void handleLogout() {
        try { SimpleClient.getClient().closeConnection(); } catch (Exception ignored) {}
        cache.clear();                // dump cached views on logout
        ClientSession.clear();
        Session.clear();
        Nav.go(ContentStack, "/il/cshaifasweng/OCSFMediatorExample/client/HomePage/HomePage.fxml");
    }

    private void handleClose() {
        Stage stage = (Stage) CloseBtn.getScene().getWindow();
        stage.close();
    }

    // ===== Session updates: keep cached tabs in sync =====

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        reinjectCustomerId();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        reinjectCustomerId();
    }

    private void reinjectCustomerId() {
        long id = ClientSession.getCustomerId();
        if (id <= 0) return;
        for (Node n : cache.values()) {
            Object c = n.getProperties().get("__controller");
            if (c instanceof RequiresSession rs) {
                rs.setCustomerId(id);
            }
        }
    }

    // Optional: call this from your destroy hook if you ever add one
    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
