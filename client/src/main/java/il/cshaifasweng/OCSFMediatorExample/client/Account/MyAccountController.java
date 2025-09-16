package il.cshaifasweng.OCSFMediatorExample.client.Account;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MyAccountController {

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton ProfileBtn;
    @FXML private ToggleButton CouponsBtn;
    @FXML private ToggleButton OrdersBtn;
    @FXML private ToggleButton PaymentsBtn;

    @FXML private Button LogOutBtn;
    @FXML private Button CloseBtn;

    @FXML private StackPane ContentStack;

    @FXML
    private void initialize() {
        // Load default view (Profile)
        loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/ProfileView.fxml");

        // Listen to navigation group changes
        navGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            if (newToggle == ProfileBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/ProfileView.fxml");
            } else if (newToggle == CouponsBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/CouponsView.fxml");
            } else if (newToggle == OrdersBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/OrdersView.fxml");
            } else if (newToggle == PaymentsBtn) {
                loadView("/il/cshaifasweng/OCSFMediatorExample/client/Account/PaymentsView.fxml");
            }
        });

        // Hook buttons
        LogOutBtn.setOnAction(e -> handleLogout());
        CloseBtn.setOnAction(e -> handleClose());
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            ContentStack.getChildren().clear();
            ContentStack.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        // TODO: clear session, go back to login scene
        System.out.println("Logging out...");
        // Example: navigate to login
        // App.setRoot("Login");  (depends on your project structure)
    }

    private void handleClose() {
        Stage stage = (Stage) CloseBtn.getScene().getWindow();
        stage.close();
    }
}
