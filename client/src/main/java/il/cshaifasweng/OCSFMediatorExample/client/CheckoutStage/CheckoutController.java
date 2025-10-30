package il.cshaifasweng.OCSFMediatorExample.client.CheckoutStage;

import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import il.cshaifasweng.OCSFMediatorExample.client.common.ClientSession;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.ValidateCouponResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.GetCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartItem;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CartState;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

public class CheckoutController {

    // Steps + badges
    @FXML private VBox step1;
    @FXML private VBox step2;
    @FXML private VBox step3;
    @FXML private Label badge1;
    @FXML private Label badge2;
    @FXML private Label badge3;

    // Method toggles
    @FXML private ToggleButton btnPickup;
    @FXML private ToggleButton btnDelivery;

    // Old optional VBoxes (not present in your FXML, so they’ll be null)
    @FXML private VBox pickupVBox;
    @FXML private VBox deliveryVBox;

    // Actual nodes in your FXML
    @FXML private GridPane pickupFields;
    @FXML private GridPane deliveryFields;

    // Resolved nodes we will show/hide safely
    private Node pickupPaneRef;
    private Node deliveryPaneRef;



    // Customer info
    @FXML private TextField FullNameText;
    @FXML private TextField PhoneText;
    @FXML private TextField EmailText;

    // Delivery fields
    @FXML private TextField CityBox;
    @FXML private TextField StreetText;
    @FXML private TextField HouseText;

    @FXML private ComboBox<String> pickupBranch;
    @FXML private DatePicker pickupDate;
    @FXML private TextField pickupTime;
    @FXML private TextField pickupPhone;

    // Gift
    @FXML private CheckBox giftCheck;
    @FXML private VBox gifVBox;
    @FXML private TextField RecipientPhoneText;
    @FXML private TextField RecepientNameText;
    @FXML private TextArea GiftNoteText;
    @FXML private DatePicker DeliveryDatePicker;
    @FXML private TextField DeliveryTimeText;

    // Payment
    @FXML private TextField CardNumberText;
    @FXML private ComboBox<String> MMBOX;
    @FXML private ComboBox<String> YYBOX;
    @FXML private TextField cvvText;
    @FXML private TextField fullNameText;
    @FXML private TextField IdNumberText;

    // Coupon UI
    @FXML private TextField CouponCodeText;
    @FXML private Button ApplyCouponBtn;
    @FXML private Label CouponStatusLabel;
    @FXML private Hyperlink RemoveCouponLink;

    // Totals
    @FXML private Label subtotalLabel;
    @FXML private Label premiumDiscountLabel;
    @FXML private Label couponDiscountLabel;
    @FXML private Label grandTotalLabel;

    // Review
    @FXML private TextArea reviewBox;

    // Footer buttons (wired by FXML)
    @FXML private Button Back1;
    @FXML private Button next1;
    @FXML private Button back3;
    @FXML private Button confirmBtn;

    // Local state
    private final ArrayList<CartItem> cartItems = new ArrayList<>();
    private boolean isPremium = false;
    private double subtotal = 0.0;
    private double premiumDiscount = 0.0;
    private double couponDiscount = 0.0;
    private double grandTotal = 0.0;
    private String appliedCouponCode = null;
    private String appliedCouponDesc = null;

    @FXML
    private void initialize() {
        ClientSession.install();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Resolve the panes we will toggle (prefer VBoxes if they exist, else GridPanes)
        pickupPaneRef   = (pickupVBox != null)    ? pickupVBox    : pickupFields;
        deliveryPaneRef = (deliveryVBox != null)  ? deliveryVBox  : deliveryFields;

        // Default view
        if (btnPickup != null) btnPickup.setSelected(true);
        if (pickupPaneRef != null)   pickupPaneRef.setVisible(true);
        if (deliveryPaneRef != null) deliveryPaneRef.setVisible(false);
        if (gifVBox != null)         gifVBox.setVisible(false);

        // Gift toggling
        if (giftCheck != null) {
            giftCheck.selectedProperty().addListener((obs, a, b) -> {
                if (gifVBox != null) gifVBox.setVisible(b);
            });
        }

        // Method toggles using resolved nodes
        if (btnPickup != null) {
            btnPickup.selectedProperty().addListener((obs, was, isNow) -> {
                if (isNow) {
                    if (pickupPaneRef != null)   pickupPaneRef.setVisible(true);
                    if (deliveryPaneRef != null) deliveryPaneRef.setVisible(false);
                }
            });
        }
        if (btnDelivery != null) {
            btnDelivery.selectedProperty().addListener((obs, was, isNow) -> {
                if (isNow) {
                    if (deliveryPaneRef != null) deliveryPaneRef.setVisible(true);
                    if (pickupPaneRef != null)   pickupPaneRef.setVisible(false);
                }
            });
        }

        // Seed pickup branches
        if (pickupBranch != null) {
            pickupBranch.getItems().setAll(
                    "Tel-Aviv Branch",
                    "Haifa Branch",
                    "Jerusalem Branch",
                    "Beersheba Branch"
            );
            if (pickupBranch.getValue() == null && !pickupBranch.getItems().isEmpty()) {
                pickupBranch.getSelectionModel().selectFirst();
            }
        }


        // Coupon actions
        if (ApplyCouponBtn != null)    ApplyCouponBtn.setOnAction(e -> doValidateCoupon());
        if (RemoveCouponLink != null)  RemoveCouponLink.setOnAction(e -> clearCoupon());

        requestAccount();
        requestCart();
    }

    private void requestAccount() {
        try { SimpleClient.getClient().sendToServer(new AccountOverviewRequest(0)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void requestCart() {
        try { SimpleClient.getClient().sendToServer(new GetCartRequest()); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void recomputeTotals() {
        subtotal = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        premiumDiscount = (isPremium && subtotal > 20.0) ? subtotal * 0.10 : 0.0;
        double afterPremium = Math.max(0.0, subtotal - premiumDiscount);
        double applied = Math.min(couponDiscount, afterPremium);
        grandTotal = Math.max(0.0, afterPremium - applied);

        setMoney(subtotalLabel, subtotal);
        setMoney(premiumDiscountLabel, -premiumDiscount);
        setMoney(couponDiscountLabel, -applied);
        setMoney(grandTotalLabel, grandTotal);

        if (reviewBox != null && reviewBox.isVisible()) {
            var sb = new StringBuilder();
            sb.append("Name: ").append(nullToDash(FullNameText)).append("\n");
            sb.append("Phone: ").append(nullToDash(PhoneText)).append("\n");
            sb.append("Email: ").append(nullToDash(EmailText)).append("\n\n");

            if (btnPickup != null && btnPickup.isSelected()) {
                sb.append("Pickup\n");
            } else {
                sb.append("Delivery to ").append(text(CityBox)).append(", ").append(text(StreetText))
                        .append(" ").append(text(HouseText)).append("\n");
                if (giftCheck != null && giftCheck.isSelected()) {
                    sb.append("Gift for ").append(text(RecepientNameText))
                            .append(" (").append(text(RecipientPhoneText)).append(")\n");
                    if (!text(GiftNoteText).isBlank()) sb.append("Note: ").append(text(GiftNoteText)).append("\n");
                }
                if ((DeliveryDatePicker != null && DeliveryDatePicker.getValue() != null) || !text(DeliveryTimeText).isBlank()) {
                    sb.append("When: ");
                    if (DeliveryDatePicker != null && DeliveryDatePicker.getValue() != null) sb.append(DeliveryDatePicker.getValue()).append(" ");
                    sb.append(text(DeliveryTimeText)).append("\n");
                }
            }

            if (appliedCouponCode != null) {
                sb.append("\nCoupon: ").append(appliedCouponCode);
                if (appliedCouponDesc != null) sb.append(" (").append(appliedCouponDesc).append(")");
                sb.append("\n");
            }

            sb.append("\nSubtotal: ").append(fmt(subtotal));
            if (premiumDiscount > 0) sb.append("\nPremium discount: -").append(fmt(premiumDiscount));
            if (couponDiscount > 0)  sb.append("\nCoupon discount:  -").append(fmt(couponDiscount));
            sb.append("\nTotal: ").append(fmt(grandTotal)).append(" USD");
            reviewBox.setText(sb.toString());
        }
    }

    private String fmt(double v) { return String.format(Locale.US, "$%.2f", v); }
    private void setMoney(Label lbl, double v) { if (lbl != null) lbl.setText(String.format(Locale.US, "$%.2f", v)); }
    private String nullToDash(TextField tf) { return tf != null && tf.getText() != null && !tf.getText().isBlank() ? tf.getText().trim() : "-"; }
    private String value(ComboBox<String> cb) { return cb != null && cb.getValue() != null ? cb.getValue() : "-"; }
    private String text(TextField tf) { return tf != null && tf.getText() != null ? tf.getText().trim() : ""; }
    private String text(TextArea ta) { return ta != null && ta.getText() != null ? ta.getText().trim() : ""; }

    private void doValidateCoupon() {
        final String code = CouponCodeText != null ? CouponCodeText.getText().trim() : "";
        if (code.isBlank()) { setCouponStatus("Enter a code."); return; }
        try { SimpleClient.getClient().sendToServer(new ValidateCouponRequest(code)); }
        catch (IOException e) { e.printStackTrace(); setCouponStatus("Failed to send."); }
    }

    private void setCouponStatus(String msg) {
        if (CouponStatusLabel != null) CouponStatusLabel.setText(msg);
    }

    private void clearCoupon() {
        appliedCouponCode = null;
        appliedCouponDesc = null;
        couponDiscount = 0.0;
        setCouponStatus("");
        if (CouponCodeText != null) CouponCodeText.clear();
        recomputeTotals();
    }

    @FXML
    private void onConfirm() {
        // Build DTO (server recomputes totals anyway)
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(ClientSession.getCustomerId());
        dto.setCreatedAt(LocalDateTime.now());
        dto.setStatus(il.cshaifasweng.OCSFMediatorExample.entities.domain.Status.PENDING);

        var items = new ArrayList<OrderItemDTO>();
        for (CartItem it : cartItems) {
            OrderItemDTO oi = new OrderItemDTO();
            oi.setSku(it.getSku());
            oi.setName(it.getName());
            oi.setQuantity(it.getQuantity());
            oi.setUnitPrice(it.getUnitPrice());
            items.add(oi);
        }
        dto.setItems(items);

        PaymentDTO pay = new PaymentDTO();
        String card = CardNumberText != null ? CardNumberText.getText().trim() : "";
        if (card.length() >= 4) pay.setCardNumberMasked("**** **** **** " + card.substring(card.length() - 4));
        pay.setCardHolderName(fullNameText != null ? fullNameText.getText() : "");
        String mm = MMBOX != null && MMBOX.getValue() != null ? MMBOX.getValue() : "";
        String yy = YYBOX != null && YYBOX.getValue() != null ? YYBOX.getValue() : "";
        pay.setExpirationDate((mm + "/" + yy).trim());
        pay.setIdNumber(IdNumberText != null ? IdNumberText.getText() : "");
        pay.setAmount(grandTotal);
        dto.setPayment(pay);

        if (btnPickup != null && btnPickup.isSelected()) {
            PickupInfoDTO pk = new PickupInfoDTO();
            if (pickupBranch != null) pk.setBranchName(pickupBranch.getValue());
            if (pickupDate != null)   pk.setPickupDate(pickupDate.getValue());
            if (pickupTime != null)   pk.setPickupTime(pickupTime.getText());
            if (pickupPhone != null)  pk.setPhone(pickupPhone.getText());
            dto.setPickup(pk);
        } else {
            DeliveryInfoDTO d = new DeliveryInfoDTO();
            d.setCity(CityBox != null ? CityBox.getText() : null); // updated to TextField
            d.setStreet(StreetText != null ? StreetText.getText() : null);
            d.setHouse(HouseText != null ? HouseText.getText() : null);
            dto.setDelivery(d);

            if (giftCheck != null && giftCheck.isSelected()) {
                GreetingCardDTO g = new GreetingCardDTO();
                g.setRecipientName(RecepientNameText != null ? RecepientNameText.getText() : "");
                g.setRecipientPhone(RecipientPhoneText != null ? RecipientPhoneText.getText() : "");
                g.setMessage(GiftNoteText != null ? GiftNoteText.getText() : "");
                dto.setGreetingCard(g);
            }
        }

        dto.setSubtotal(subtotal);
        dto.setDiscountTotal(premiumDiscount + couponDiscount);
        dto.setTotal(grandTotal);

        try { SimpleClient.getClient().sendToServer(new ConfirmRequest(dto, appliedCouponCode)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // -------- EventBus subscribers --------

    @Subscribe
    public void onAccountOverview(AccountOverviewResponse r) {
        Platform.runLater(() -> {
            if (r != null && r.ok() && r.customer() != null) {
                this.isPremium = r.customer().isPremium();
                // Prefill identity fields if blank
                var c = r.customer();
                if (FullNameText != null && (FullNameText.getText() == null || FullNameText.getText().isBlank())) {
                    FullNameText.setText(c.getDisplayName());
                }
                if (EmailText != null && (EmailText.getText() == null || EmailText.getText().isBlank())) {
                    EmailText.setText(c.getEmail());
                }
                if (PhoneText != null && (PhoneText.getText() == null || PhoneText.getText().isBlank())) {
                    PhoneText.setText(c.getPhone());
                }
                recomputeTotals();
            }
        });
    }


    @Subscribe
    public void onCartState(CartState s) {
        Platform.runLater(() -> {
            cartItems.clear();
            if (s != null && s.getItems() != null) cartItems.addAll(s.getItems());
            recomputeTotals();
        });
    }

    @Subscribe
    public void onCouponValidated(ValidateCouponResponse r) {
        Platform.runLater(() -> {
            if (r == null) return;
            if (!r.isValid()) {
                setCouponStatus(r.getMessage() != null ? r.getMessage() : "Invalid code");
                clearCoupon();
                return;
            }
            appliedCouponCode = r.getCode();
            if ("PERCENT".equals(r.getDiscountType())) {
                appliedCouponDesc = (int) r.getAmount() + "% off";
                double afterPremium = Math.max(0.0, subtotal - premiumDiscount);
                couponDiscount = afterPremium * (r.getAmount() / 100.0);
            } else {
                appliedCouponDesc = "$" + String.format(Locale.US, "%.0f", r.getAmount()) + " off";
                couponDiscount = r.getAmount();
            }
            setCouponStatus("Applied: " + appliedCouponDesc);
            recomputeTotals();
        });
    }

    @Subscribe
    public void onConfirmResponse(ConfirmResponse r) {
        Platform.runLater(() -> {
            if (r == null) return;
            if (r.isSuccess()) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION, r.getMessage());
                ok.setHeaderText("Order placed");
                ok.showAndWait();
            } else {
                Alert er = new Alert(Alert.AlertType.ERROR, r.getMessage());
                er.setHeaderText("Checkout failed");
                er.showAndWait();
            }
        });
    }

    // ---------- Step navigation handlers (match FXML onAction) ----------
    @FXML private void handleBack1() { setStep(1); }
    @FXML private void handleNext1() { setStep(2); }
    @FXML private void handleBack2() { setStep(1); }
    @FXML private void handlePay()   { setStep(3); }
    @FXML private void handleBack3() { setStep(2); }
    @FXML private void handleConfirm(){ onConfirm(); }

    // ---------- Step UI helpers ----------
    private void setStep(int n) {
        if (step1 != null) step1.setVisible(n == 1);
        if (step2 != null) step2.setVisible(n == 2);
        if (step3 != null) step3.setVisible(n == 3);
        setActiveBadge(n);
        recomputeTotals();
    }

    private void setActiveBadge(int n) {
        if (badge1 != null) badge1.getStyleClass().remove("active");
        if (badge2 != null) badge2.getStyleClass().remove("active");
        if (badge3 != null) badge3.getStyleClass().remove("active");
        if (n == 1 && badge1 != null && !badge1.getStyleClass().contains("active")) badge1.getStyleClass().add("active");
        if (n == 2 && badge2 != null && !badge2.getStyleClass().contains("active")) badge2.getStyleClass().add("active");
        if (n == 3 && badge3 != null && !badge3.getStyleClass().contains("active")) badge3.getStyleClass().add("active");
    }
}
