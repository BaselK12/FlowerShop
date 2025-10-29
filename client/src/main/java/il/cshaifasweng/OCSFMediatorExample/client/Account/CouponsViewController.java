package il.cshaifasweng.OCSFMediatorExample.client.Account;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.client.common.RequiresSession;
import il.cshaifasweng.OCSFMediatorExample.client.ui.ViewTracker;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.CouponDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CouponsViewController implements RequiresSession {

    @FXML private Label couponsTitleLabel;
    @FXML private Label couponsCountLabel;
    @FXML private FlowPane couponsFlow;
    @FXML private ProgressIndicator couponsLoading;
    @FXML private Button btnLoadMore;
    @FXML private ScrollPane couponsScroll;

    private long customerId = 0;
    private final List<CouponDTO> all = new ArrayList<>();
    private int page = 0;
    private final int size = 10;
    private int totalCount = 0;

    @FXML
    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // ScrollPane + FlowPane layout sanity
        if (couponsScroll != null) {
            couponsScroll.setFitToWidth(true);
            couponsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            couponsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Make FlowPane wrap to the viewport width so cards flow and vertical scroll appears
            couponsScroll.viewportBoundsProperty().addListener((obs, oldV, newV) -> {
                // subtract a little padding so cards don’t clip the scrollbar
                if (couponsFlow != null) {
                    couponsFlow.setPrefWrapLength(Math.max(0, newV.getWidth() - 16));
                }
            });
        }

        if (couponsFlow != null) {
            couponsFlow.setHgap(12);
            couponsFlow.setVgap(12);
            couponsFlow.setPadding(new Insets(8));
            // sensible initial wrap length before first layout pass
            couponsFlow.setPrefWrapLength(520);
        }

        btnLoadMore.setOnAction(e -> loadNextPage());
        btnLoadMore.setVisible(false);
        couponsLoading.setVisible(false);
        couponsCountLabel.setText("(0)");

        // NEW: if we landed here already logged in, start loading immediately
        long id = ClientSession.getCustomerId();
        if (id > 0) {
            setCustomerId(id); // reuse your existing flow
        }
    }

    @Override
    public void setCustomerId(long customerId) {
        this.customerId = customerId;
        resetAndLoad();
    }

    private void resetAndLoad() {
        page = 0;
        totalCount = 0;
        all.clear();
        if (couponsFlow != null) couponsFlow.getChildren().clear();
        requestPage(0);
    }

    private void loadNextPage() {
        if (all.size() < totalCount) {
            requestPage(page + 1);
        }
    }

    private void requestPage(int p) {
        couponsLoading.setVisible(true);
        try {
            // server infers customer from connection; no need to ship customerId
            SimpleClient.getClient().sendToServer(new GetCouponsRequest(p, size));
        } catch (Exception e) {
            e.printStackTrace();
            couponsLoading.setVisible(false);
            showError("Failed to request coupons: " + e.getMessage());
        }
    }

    @Subscribe
    public void onCoupons(GetCouponsResponse resp) {
        Platform.runLater(() -> {
            try {
                if (resp == null) return;

                if (resp.getPage() == 0) {
                    if (couponsFlow != null) couponsFlow.getChildren().clear();
                    all.clear();
                }

                page = resp.getPage();
                totalCount = resp.getTotalCount();
                all.addAll(resp.getItems());

                // Render only the newly arrived page to avoid duplicates
                render(resp.getItems());

                couponsCountLabel.setText("(" + totalCount + ")");
                couponsLoading.setVisible(false);
                btnLoadMore.setVisible(all.size() < totalCount);
            } catch (Exception e) {
                e.printStackTrace();
                couponsLoading.setVisible(false);
                showError("Failed to render coupons.");
            }
        });
    }

    // NEW: if login completes while we're open, kick off loading
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogin(LoginResponse r) {
        if (r == null || !r.isOk()) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) setCustomerId(id);
    }

    // NEW: when overview arrives (hydrated customer), refresh
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOverview(AccountOverviewResponse r) {
        if (r == null || !r.isOk() || r.getCustomer() == null) return;
        long id = ClientSession.getCustomerId();
        if (id > 0) setCustomerId(id);
    }

    // NEW: optional, when this view becomes active and nothing loaded yet, fetch page 0
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActive(ViewTracker.ActiveControllerChanged e) {
        if (e == null) return;
        if ("CouponsView".equals(e.controllerId) && customerId > 0 && all.isEmpty()) {
            resetAndLoad();
        }
    }

    private void render(List<CouponDTO> items) {
        if (items == null || items.isEmpty() || couponsFlow == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (CouponDTO c : items) {
            VBox card = new VBox(6);
            card.setPrefWidth(300);
            card.setPrefHeight(110);
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 12; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 0);"
            );

            Label title = new Label(c.getTitle());
            title.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");

            var exp = c.getExpiration() != null ? c.getExpiration().format(fmt) : "—";
            Label expiry = new Label("Valid until " + exp);
            expiry.setStyle("-fx-text-fill: #6b7a90;");

            Label status = new Label(c.isActive() ? "Active" : "Expired");
            status.setStyle(c.isActive()
                    ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 8; -fx-font-size: 12px;"
                    : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 8; -fx-font-size: 12px;"
            );

            if (!c.isActive()) card.setOpacity(0.6);

            card.getChildren().addAll(title, expiry, status);
            couponsFlow.getChildren().add(card);
        }
    }

    private static void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    // Optional cleanup if you close/destroy this controller explicitly
    public void onClose() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
