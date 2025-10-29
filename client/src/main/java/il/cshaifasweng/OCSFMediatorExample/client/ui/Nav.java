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

/** Simple navigator with history + view tracking. */
public final class Nav {
    private static final Deque<Parent> history = new ArrayDeque<>();

    private Nav() {}

    /** Load FXML at absolute path (e.g. "/il/.../HomePage/HomePage.fxml"). */
    public static void go(Node fromAnyNode, String fxmlPath) {
        Objects.requireNonNull(fromAnyNode, "fromAnyNode");
        Objects.requireNonNull(fxmlPath, "fxmlPath");

        Stage stage = (Stage) fromAnyNode.getScene().getWindow();
        Scene scene = stage.getScene();
        Parent currentRoot = scene.getRoot();

        Parent nextRoot = load(fxmlPath);

        // Push current to history, swap root
        history.push(currentRoot);
        scene.setRoot(nextRoot);

        // If controller exists, announce it (uses fx:controller class name)
        Object controller = getController(nextRoot);
        String controllerId = extractControllerId(controller, fxmlPath);
        ViewTracker.setActive(controllerId, controller);
    }

    public static void back(Node fromAnyNode) {
        if (history.isEmpty()) return;
        Stage stage = (Stage) fromAnyNode.getScene().getWindow();
        Parent prev = history.pop();
        stage.getScene().setRoot(prev);

        Object controller = getController(prev); // may be null if first screen wasn't loaded via Nav
        String id = extractControllerId(controller, "history");
        ViewTracker.setActive(id, controller);
    }

    public static void clearHistory() { history.clear(); }

    // --------------------------

    private static Parent load(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Nav.class.getResource(fxmlPath));
            Parent root = loader.load();
            // stash controller for later retrieval (including Back)
            root.getProperties().put("__controller", loader.getController());
            return root;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: " + fxmlPath, e);
        }
    }


    private static String extractControllerId(Object controller, String fxmlPath) {
        if (controller != null) {
            String simple = controller.getClass().getSimpleName();
            return simple.endsWith("Controller") ? simple.substring(0, simple.length() - "Controller".length()) : simple;
        }
        // Fallback to FXML name
        int slash = fxmlPath.lastIndexOf('/');
        String name = slash >= 0 ? fxmlPath.substring(slash + 1) : fxmlPath;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getController(Parent root) {
        return (T) root.getProperties().get("__controller");
    }

}
