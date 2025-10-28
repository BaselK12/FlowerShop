package il.cshaifasweng.OCSFMediatorExample.client.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class Nav {
    private static final Deque<Parent> history = new ArrayDeque<>();

    private Nav() {}


    public static void go(Node anyNodeInWindow, String fxmlPath) {
        Platform.runLater(() -> {
            try {
                Parent root = FXMLLoader.load(Nav.class.getResource(fxmlPath));

                Window window = resolveWindow(anyNodeInWindow);
                if (window == null) {
                    // no window yet? fine, make one.
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();
                    return;
                }

                if (window instanceof Stage stage) {
                    Scene scene = stage.getScene();
                    if (scene == null) {
                        stage.setScene(new Scene(root));
                    } else {
                        scene.setRoot(root);
                    }
                    // ensure visible
                    if (!stage.isShowing()) stage.show();
                } else {
                    // extremely rare, but handle anyway
                    Stage stage = (Stage) window;
                    Scene scene = stage.getScene();
                    if (scene == null) stage.setScene(new Scene(root));
                    else scene.setRoot(root);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
            }
        });
    }

    private static Window resolveWindow(Node n) {
        if (n != null) {
            var sc = n.getScene();
            if (sc != null) return sc.getWindow();
        }
        for (Window w : Window.getWindows()) {
            if (w.isShowing()) return w;
        }
        return null;
    }

    public static void back(Node fromAnyNode) {
        if (history.isEmpty()) return;
        Stage stage = (Stage) fromAnyNode.getScene().getWindow();
        Parent prev = history.pop();
        stage.getScene().setRoot(prev);
    }


    public static void clearHistory() {
        history.clear();
    }
}
