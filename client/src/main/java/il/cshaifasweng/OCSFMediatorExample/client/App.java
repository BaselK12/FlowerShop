package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.URL;

public class App extends Application {
    private static Scene scene;
    private static SimpleClient client;
    private static Stage mainStage;


    @Override
    public void start(Stage stage) throws Exception {

        // ## DO NOT TOUCH OR CHANGE ## //
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Server IP");
        dialog.setHeaderText("Connect to Server");
        dialog.setContentText("Please enter the server IP address:");
        String serverIP = dialog.showAndWait().orElse("localhost");

        client = new SimpleClient(serverIP, 3050);
        client.openConnection();
        SimpleClient.setClient(client);
        GlobalErrors.install();


        // boot the first view
//        scene = new Scene(loadFXML("/il/cshaifasweng/OCSFMediatorExample/client/CustomerLoginPage.fxml"));
//        stage.setScene(scene);
//        stage.setTitle("FlowerShop");
//        stage.show();


        // load the create bouquet-+
        scene = new Scene(loadFXML("/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminLoginPage.fxml"));
        stage.setScene(scene);
        stage.setTitle("FlowerShop");
        stage.show();

    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        URL url;
        if (fxml.startsWith("/")) {
            url = App.class.getResource(fxml.endsWith(".fxml") ? fxml : fxml + ".fxml");
        } else {
            // default base folder for your client FXMLs; adjust if needed
            String base = "/il/cshaifasweng/OCSFMediatorExample/client/";
            url = App.class.getResource(base + (fxml.endsWith(".fxml") ? fxml : fxml + ".fxml"));
        }
        if (url == null) throw new IllegalStateException("FXML not found: " + fxml);
        return FXMLLoader.load(url);
    }

    public static SimpleClient getClient() { return SimpleClient.getClient(); }

    public static void main(String[] args) { launch(args); }
}
