package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CartUpdateRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.RemoveFromCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class CartItemCardController {
    @FXML private Label NameLabel;
    @FXML private Label UnitPriceLabel;
    @FXML private Label SubTotalLabel;
    @FXML private Spinner<Integer> QtySpinner;
    @FXML private Button RemoveBtn;
    @FXML private ImageView ProductImage;

    private CartItem item;
    private Runnable onDelete;   // parent callback to refresh list after delete
    private Runnable onUpdate;   // parent callback to refresh totals after qty change

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
    private final PauseTransition debounce = new PauseTransition(Duration.millis(200));
    private volatile boolean removing = false;

    @FXML
    private void initialize() {
        // Spinner programmatic setup; min=0 enables "remove on zero"
        if (QtySpinner != null) {
            QtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 1));
            QtySpinner.valueProperty().addListener((obs, oldV, newV) -> {
                if (item == null || removing) return;
                debounce.stop();
                debounce.setOnFinished(ev -> {
                    if (newV == null) return;
                    if (newV <= 0) {
                        // zero means remove
                        onRemove(null);
                    } else {
                        // update qty
                        item.setQuantity(newV); // client DTO clamps >=1, but we only get here for >=1
                        sendUpdate(item);
                        if (onUpdate != null) onUpdate.run();
                        updateSubtotal();
                    }
                });
                debounce.playFromStart();
            });
        }
    }

    void setData(CartItem item, Runnable onDelete, Runnable onUpdate) {
        this.item = item;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;

        if (NameLabel != null) NameLabel.setText(item.getName() != null ? item.getName() : item.getSku());
        if (UnitPriceLabel != null) UnitPriceLabel.setText(currency.format(item.getUnitPrice()));
        if (QtySpinner != null) QtySpinner.getValueFactory().setValue(Math.max(0, item.getQuantity()));
        updateSubtotal();

        if (ProductImage != null && item.getPictureUrl() != null && !item.getPictureUrl().isBlank()) {
            try {
                ProductImage.setImage(new Image(item.getPictureUrl(), true));
            } catch (Exception ignored) {}
        }
    }

    private void updateSubtotal() {
        if (SubTotalLabel != null) {
            double subtotal = item.getUnitPrice() * Math.max(0, QtySpinner.getValue());
            SubTotalLabel.setText(currency.format(subtotal));
        }
    }

    private void sendUpdate(CartItem it) {
        try {
            SimpleClient.getClient().sendToServer(new CartUpdateRequest(it));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRemove(ActionEvent e) {
        if (item == null) return;
        removing = true;
        try {
            SimpleClient.getClient().sendToServer(new RemoveFromCartRequest(item.getSku()));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Optimistic UI: let the parent refresh after server ack
            if (onDelete != null) onDelete.run();
            removing = false;
        }
    }
}
