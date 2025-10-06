package il.cshaifasweng.OCSFMediatorExample.client.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class Nav {
    private static final Deque<Parent> history = new ArrayDeque<>();

    private Nav() {}


    public static void go(Node fromAnyNode, String fxmlPathOnClasspath) {
        try {
            Stage stage = (Stage) fromAnyNode.getScene().getWindow();
            Parent currentRoot = stage.getScene().getRoot();
            history.push(currentRoot);

            Parent next = FXMLLoader.load(Objects.requireNonNull(
                    Nav.class.getResource(fxmlPathOnClasspath),
                    "FXML not found: " + fxmlPathOnClasspath
            ));
            stage.getScene().setRoot(next);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxmlPathOnClasspath, e);
        }
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
