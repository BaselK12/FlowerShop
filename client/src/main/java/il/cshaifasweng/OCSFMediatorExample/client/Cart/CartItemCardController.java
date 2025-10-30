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

    // Prevent the listener from firing while we populate the UI
    private boolean suppressQtyEvents = false;

    @FXML
    private void initialize() {
        // Allow zero to mean "remove"
        if (QtySpinner != null) {
            QtySpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 1));

            QtySpinner.valueProperty().addListener((obs, oldV, newV) -> {
                if (suppressQtyEvents || item == null || removing) return;
                if (newV != null && oldV != null && newV.equals(oldV)) return;

                debounce.stop();
                debounce.setOnFinished(ev -> {
                    if (newV == null) return;

                    if (newV <= 0) {
                        // Optimistic UI: zero out subtotal and ping parent total before server roundtrip
                        if (SubTotalLabel != null) SubTotalLabel.setText(currency.format(0));
                        if (onUpdate != null) onUpdate.run();
                        onRemove(null);  // will also trigger parent onDelete
                    } else {
                        // Update local model first so CartController total uses the new qty immediately
                        item.setQuantity(newV);
                        updateSubtotal();
                        if (onUpdate != null) onUpdate.run();
                        sendUpdate(item);
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

        suppressQtyEvents = true;
        try {
            if (NameLabel != null) NameLabel.setText(item.getName() != null ? item.getName() : item.getSku());
            if (UnitPriceLabel != null) UnitPriceLabel.setText(currency.format(item.getUnitPrice()));
            if (QtySpinner != null) {
                int q = Math.max(0, item.getQuantity());  // allow 0 path
                QtySpinner.getValueFactory().setValue(q);
            }
            updateSubtotal();

            if (ProductImage != null && item.getPictureUrl() != null && !item.getPictureUrl().isBlank()) {
                try {
                    ProductImage.setImage(new Image(item.getPictureUrl(), true));
                } catch (Exception ignored) { }
            }
        } finally {
            suppressQtyEvents = false;
        }
    }

    private void updateSubtotal() {
        if (SubTotalLabel != null && QtySpinner != null && item != null) {
            int qty = QtySpinner.getValue() == null ? Math.max(0, item.getQuantity()) : QtySpinner.getValue();
            double subtotal = item.getUnitPrice() * Math.max(0, qty);
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
            // Tell parent immediately; it will adjust list/total and also re-sync from server
            if (onDelete != null) onDelete.run();
            removing = false;
        }
    }
}
