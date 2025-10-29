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

    @FXML
    private void initialize() {
        // Spinner is programmatically controlled; prevent free text that can cause exceptions
        if (QtySpinner != null) {
            QtySpinner.setEditable(false);
        }
    }

    public void setData(CartItem item, Runnable onDelete, Runnable onUpdate) {
        this.item = item;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;

        if (NameLabel != null) {
            NameLabel.setText(item.getName() != null ? item.getName() : "");
        }
        if (UnitPriceLabel != null) {
            UnitPriceLabel.setText(currency.format(item.getUnitPrice()));
        }

        // load image defensively
        if (ProductImage != null) {
            try {
                String url = item.getPictureUrl();
                if (url != null && !url.isBlank()) {
                    Image img = new Image(url, true);
                    // If it fails, we just leave the ImageView as-is
                    ProductImage.setImage(img);
                } else {
                    ProductImage.setImage(null);
                }
            } catch (Exception ignored) { /* no-op */ }
        }

        int qty = Math.max(1, item.getQuantity());
        if (QtySpinner != null) {
            QtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, qty));
        }
        updateSubtotal();

        // debounce quantity changes so we don't spam the server
        if (QtySpinner != null) {
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
        if (SubTotalLabel != null) {
            SubTotalLabel.setText(currency.format(item.getSubtotal()));
        }
    }

    @FXML
    private void onRemove(ActionEvent e) {
        if (removing) return;
        removing = true;

        // make sure debounce doesn't send an outdated qty
        debounce.stop();

        // Temporarily lock UI on this card to avoid double actions
        setControlsDisabled(true);

        // optimistic UI: remove immediately
        if (onDelete != null) onDelete.run();

        // tell server this SKU is gone by setting quantity to zero on the SAME item
        try {
            item.setQuantity(0);
            SimpleClient.getClient().sendToServer(new CartUpdateRequest(item));
        } catch (IOException ex) {
            ex.printStackTrace();
            // If you want to roll back UI on failure, you can re-enable:
            // setControlsDisabled(false);
        } finally {
            removing = false;
        }
    }

    private void setControlsDisabled(boolean disabled) {
        if (RemoveBtn != null) RemoveBtn.setDisable(disabled);
        if (QtySpinner != null) QtySpinner.setDisable(disabled);
    }
}
