package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import il.cshaifasweng.OCSFMediatorExample.client.net.ClientBridge;
import il.cshaifasweng.OCSFMediatorExample.client.bus.ClientBus;
import il.cshaifasweng.OCSFMediatorExample.client.bus.events.SendMessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Ping;

import java.io.IOException;
import java.net.URL;

public class App extends Application {
    private static Scene scene;
    private static ClientBridge NET;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("FlowerShop Client");
        stage.show();

        NET = new ClientBridge("127.0.0.1", 3000);

        Platform.runLater(() -> {
            System.out.println("[CLIENT] posting Ping");
            ClientBus.get().post(new SendMessageEvent(new Ping("smoke")));
        });
        ClientBus.get().post(new SendMessageEvent(
                new il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest("alice","alice123")
        ));
    }

    public static void setRoot(String fxml) throws IOException { scene.setRoot(loadFXML(fxml)); }

    private static Parent loadFXML(String name) throws IOException {
        URL url = App.class.getResource(name + ".fxml");
        if (url == null) throw new IOException("FXML not found: " + name + ".fxml");
        return new FXMLLoader(url).load();
    }

    public static void main(String[] args) { launch(args); }
}
