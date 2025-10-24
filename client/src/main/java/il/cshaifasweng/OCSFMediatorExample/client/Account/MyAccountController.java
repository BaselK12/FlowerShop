package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.client.common.Session;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyAccountController {

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton ProfileBtn;
    @FXML private ToggleButton CouponsBtn;
    @FXML private ToggleButton OrdersBtn;
    @FXML private ToggleButton PaymentsBtn;

    @FXML private Button LogOutBtn;
    @FXML private Button CloseBtn;

    @FXML private StackPane ContentStack;

    // Cache loaded FXMLs so switching tabs doesn’t re-parse files every time
    private final Map<String, Node> cache = new HashMap<>();

    @FXML
    private void initialize() {
        ClientSession.install();
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
                    long id = ClientSession.getCustomerId();  // << use the cached id
                    rs.setCustomerId(id);
                }
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
        ClientSession.clear();
        Session.clear();
        Nav.go(ContentStack, "/il/cshaifasweng/OCSFMediatorExample/client/Customer/CustomerLoginPage.fxml");
    }

    private void handleClose() {
        Stage stage = (Stage) CloseBtn.getScene().getWindow();
        stage.close();
    }
}
