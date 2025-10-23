package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.ErrorResponse;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public final class GlobalErrors {
    private static boolean installed;

    private GlobalErrors() {}

    public static void install() {
        if (installed) return;
        EventBus.getDefault().register(new GlobalErrors());
        installed = true;
    }

    @Subscribe
    public void onError(ErrorResponse err) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Server error");
            a.setHeaderText("The server couldn’t complete your request");
            a.setContentText(err.reason());
            a.showAndWait();
        });
    }
}
