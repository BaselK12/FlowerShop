package il.cshaifasweng.OCSFMediatorExample.client.Account;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;

public class CouponsViewController {

    @FXML private Label couponsTitleLabel;
    @FXML private Label couponsCountLabel;
    @FXML private FlowPane couponsFlow;
    @FXML private ProgressIndicator couponsLoading;
    @FXML private Button btnLoadMore;

    private int totalCount = 0;

    @FXML
    public void initialize() {
        loadCouponsFromServer(0, 10); // first page, 10 per load
    }

    private void loadCouponsFromServer(int page, int size) {
        couponsLoading.setVisible(true);

        // TODO: Replace with your async server call via SimpleClient
        Platform.runLater(() -> {
            // mock example
            List<Coupon> coupons = List.of(
                    new Coupon("15% Off", "2025-12-31", true),
                    new Coupon("10% Off", "2024-10-10", false)
            );
            totalCount = coupons.size();
            updateCoupons(coupons);
        });
    }

    private void updateCoupons(List<Coupon> coupons) {
        couponsFlow.getChildren().clear();
        for (Coupon c : coupons) {
            couponsFlow.getChildren().add(createCouponCard(c));
        }
        couponsCountLabel.setText("(" + totalCount + ")");
        couponsLoading.setVisible(false);
    }

    private Pane createCouponCard(Coupon c) {
        VBox card = new VBox(6);
        card.setPrefWidth(300);
        card.setPrefHeight(110);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 0);");

        Label title = new Label(c.title);
        title.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        Label expiry = new Label("Valid until " + c.expiration);
        expiry.setStyle("-fx-text-fill: #6b7a90;");

        Label status = new Label(c.active ? "Active" : "Expired");
        status.setStyle(
                c.active
                        ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 8; -fx-font-size: 12px;"
                        : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 8; -fx-font-size: 12px;"
        );

        if (!c.active) {
            card.setOpacity(0.6);
        }

        card.getChildren().addAll(title, expiry, status);
        return card;
    }

    // Mock Coupon class (replace with your real entity)
    static class Coupon {
        String title;
        String expiration;
        boolean active;
        Coupon(String t, String e, boolean a) { title = t; expiration = e; active = a; }
    }
}
