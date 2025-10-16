package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CartUpdateRequest;
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

    @FXML private ImageView ProductImage;
    @FXML private Spinner<Integer> QtySpinner;
    @FXML private Button RemoveBtn;
    @FXML private Label SubTotalLabel;
    @FXML private Label UnitPriceLabel;
    @FXML private Label NameLabel;

    private CartItem item;
    private Runnable onDelete;   // parent callback to remove this card from UI
    private Runnable onUpdate;   // parent callback to refresh totals

    // helpers
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("he", "IL"));
    private final PauseTransition debounce = new PauseTransition(Duration.millis(200));
    private volatile boolean removing = false;

    public void setData(CartItem item, Runnable onDelete, Runnable onUpdate) {
        this.item = item;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;

        NameLabel.setText(item.getName());
        UnitPriceLabel.setText(currency.format(item.getUnitPrice()));

        // load image defensively
        try {
            String url = item.getPictureUrl();
            if (url != null && !url.isBlank()) {
                Image img = new Image(url, true);
                // if it fails, we just leave the ImageView empty
                img.errorProperty().addListener((obs, wasErr, isErr) -> { /* no-op */ });
                ProductImage.setImage(img);
            }
        } catch (Exception ignored) { }

        int qty = Math.max(1, item.getQuantity());
        QtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, qty));
        updateSubtotal();

        // debounce quantity changes so we don't spam the server
        QtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || removing) return;
            item.setQuantity(newVal);
            updateSubtotal();
            if (onUpdate != null) onUpdate.run();

            // restart debounce timer
            debounce.stop();
            debounce.setOnFinished(e -> sendQtyUpdate());
            debounce.playFromStart();
        });
    }

    private void sendQtyUpdate() {
        try {
            SimpleClient.getClient().sendToServer(new CartUpdateRequest(item));
        } catch (IOException ex) {
            // If sending fails, the next CartState from server will reconcile us anyway.
            ex.printStackTrace();
        }
    }

    private void updateSubtotal() {
        SubTotalLabel.setText(currency.format(item.getSubtotal()));
    }

    @FXML
    private void onRemove(ActionEvent e) {
        if (removing) return;
        removing = true;

        // make sure debounce doesn't send an outdated qty
        debounce.stop();

        // optimistic UI: remove immediately
        if (onDelete != null) onDelete.run();

        // tell server this SKU is gone by setting quantity to zero on the SAME item
        try {
            item.setQuantity(0);
            SimpleClient.getClient().sendToServer(new CartUpdateRequest(item));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            removing = false;
        }
    }
}
