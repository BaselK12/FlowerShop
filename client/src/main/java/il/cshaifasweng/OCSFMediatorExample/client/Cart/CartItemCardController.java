package il.cshaifasweng.OCSFMediatorExample.client.Cart;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CartUpdateRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.event.ActionEvent;

import java.io.IOException;


public class CartItemCardController {

    @FXML private ImageView ProductImage;

    @FXML private Spinner<Integer> QtySpinner;

    @FXML private Button RemoveBtn;

    @FXML private Label SubTotalLabel;
    @FXML private Label UnitPriceLabel;
    @FXML private Label NameLabel;


    private CartItem item;
    private Runnable onDelete;
    private Runnable onUpdate;


    public void setData(CartItem item, Runnable onDelete, Runnable onUpdate) {
        this.item = item;
        this.onDelete = onDelete;
        this.onUpdate = onUpdate;

        NameLabel.setText(item.getName());
        UnitPriceLabel.setText("₪" + item.getUnitPrice());
        ProductImage.setImage(new Image(item.getPictureUrl()));

        QtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, item.getQuantity()));
        updateSubtotal();

        QtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            item.setQuantity(newVal);
            updateSubtotal();
            if (onUpdate != null) onUpdate.run();

            try {
                SimpleClient.getClient().sendToServer(new CartUpdateRequest(item));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }


    private void updateSubtotal() {
        SubTotalLabel.setText(String.format("₪%.2f", item.getSubtotal()));
    }

    @FXML
    private void onRemove(ActionEvent e) {
        if (onDelete != null) onDelete.run();

        try {
            SimpleClient.getClient().sendToServer(new CartUpdateRequest(item));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}