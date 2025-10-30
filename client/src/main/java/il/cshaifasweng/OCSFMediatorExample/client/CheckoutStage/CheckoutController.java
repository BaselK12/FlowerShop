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
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.StoreOption;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresResponse;
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
import java.time.Year;
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

    // Optional VBoxes (if your FXML has them)
    @FXML private VBox pickupVBox;
    @FXML private VBox deliveryVBox;

    // Actual nodes in your FXML
    @FXML private GridPane pickupFields;
    @FXML private GridPane deliveryFields;

    // Resolved nodes we will show/hide safely
    private Node pickupPaneRef;
    private Node deliveryPaneRef;

    // Stores
    private static final long DEFAULT_DELIVERY_STORE_ID = 1L;
    private java.util.List<StoreOption> storeOptions = new java.util.ArrayList<>();
    private boolean storesLoaded = false;
    private Long resolvedDefaultStoreId = null;

    // Customer info
    @FXML private TextField FullNameText;
    @FXML private TextField PhoneText;
    @FXML private TextField EmailText;

    // Delivery fields (CityBox is a TextField now)
    @FXML private TextField CityBox;
    @FXML private TextField StreetText;
    @FXML private TextField HouseText;

    // Pickup fields
    @FXML private ComboBox<StoreOption> pickupBranch;
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
        pickupPaneRef   = (pickupVBox != null)   ? pickupVBox   : pickupFields;
        deliveryPaneRef = (deliveryVBox != null) ? deliveryVBox : deliveryFields;

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

        // Request real stores from server
        try {
            SimpleClient.getClient().sendToServer(new GetStoresRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fallback seed (will be replaced when GetStoresResponse arrives)
        if (pickupBranch != null && pickupBranch.getItems().isEmpty()) {
            pickupBranch.getItems().setAll(
                    new StoreOption("101", "Tel-Aviv Branch"),
                    new StoreOption("102", "Haifa Branch"),
                    new StoreOption("103", "Jerusalem Branch"),
                    new StoreOption("104", "Beersheba Branch")
            );
            if (pickupBranch.getValue() == null && !pickupBranch.getItems().isEmpty()) {
                pickupBranch.getSelectionModel().selectFirst();
            }
        }

        // Seed expiration dropdowns
        fillExpiryBoxes();

        // Coupon actions
        if (ApplyCouponBtn != null)    ApplyCouponBtn.setOnAction(e -> doValidateCoupon());
        if (RemoveCouponLink != null)  RemoveCouponLink.setOnAction(e -> clearCoupon());

        requestAccount();
        requestCart();
    }

    private void fillExpiryBoxes() {
        if (MMBOX != null && MMBOX.getItems().isEmpty()) {
            for (int m = 1; m <= 12; m++) {
                MMBOX.getItems().add(String.format("%02d", m));
            }
        }
        if (YYBOX != null && YYBOX.getItems().isEmpty()) {
            int y = Year.now().getValue() % 100;
            for (int i = 0; i < 15; i++) {
                YYBOX.getItems().add(String.format("%02d", (y + i) % 100));
            }
        }
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

        // Build review text when review is visible
        if (reviewBox != null && reviewBox.isVisible()) {
            reviewBox.setText(buildReviewText());
        }
    }

    private String buildReviewText() {
        StringBuilder sb = new StringBuilder();

        // Customer info first
        sb.append("Name: ").append(nullToDash(FullNameText)).append("\n");
        sb.append("Phone: ").append(nullToDash(PhoneText)).append("\n");
        sb.append("Email: ").append(nullToDash(EmailText)).append("\n\n");

        // Method details
        if (btnPickup != null && btnPickup.isSelected()) {
            sb.append("Pickup\n");
            sb.append("  Branch: ").append(value(pickupBranch)).append("\n");
            if (pickupDate != null && pickupDate.getValue() != null) {
                sb.append("  Date: ").append(pickupDate.getValue()).append("\n");
            }
            sb.append("  Time: ").append(text(pickupTime)).append("\n");
            sb.append("  Phone: ").append(text(pickupPhone)).append("\n");
        } else {
            sb.append("Delivery\n");
            sb.append("  Address: ").append(text(CityBox)).append(", ")
                    .append(text(StreetText)).append(" ").append(text(HouseText)).append("\n");
            if ((DeliveryDatePicker != null && DeliveryDatePicker.getValue() != null) || !text(DeliveryTimeText).isBlank()) {
                sb.append("  When: ");
                if (DeliveryDatePicker != null && DeliveryDatePicker.getValue() != null) sb.append(DeliveryDatePicker.getValue()).append(" ");
                sb.append(text(DeliveryTimeText)).append("\n");
            }
            if (giftCheck != null && giftCheck.isSelected()) {
                sb.append("  Gift for: ").append(text(RecepientNameText))
                        .append(" (").append(text(RecipientPhoneText)).append(")\n");
                if (!text(GiftNoteText).isBlank()) sb.append("  Note: ").append(text(GiftNoteText)).append("\n");
            }
        }

        // Items
        sb.append("\nItems:\n");
        for (CartItem it : cartItems) {
            double line = it.getUnitPrice() * it.getQuantity();
            sb.append("  ").append(it.getName() != null ? it.getName() : it.getSku())
                    .append(" — ").append(fmt(line)).append("\n");
        }

        // Discounts & totals
        if (appliedCouponCode != null) {
            sb.append("\nCoupon: ").append(appliedCouponCode);
            if (appliedCouponDesc != null) sb.append(" (").append(appliedCouponDesc).append(")");
            sb.append("\n");
        }

        sb.append("\nSubtotal: ").append(fmt(subtotal));
        if (premiumDiscount > 0) sb.append("\nPremium discount: -").append(fmt(premiumDiscount));
        if (couponDiscount > 0)  sb.append("\nCoupon discount:  -").append(fmt(couponDiscount));
        sb.append("\nTotal: ").append(fmt(grandTotal)).append(" USD");

        return sb.toString();
    }

    private String fmt(double v) { return String.format(Locale.US, "$%.2f", v); }
    private void setMoney(Label lbl, double v) { if (lbl != null) lbl.setText(String.format(Locale.US, "$%.2f", v)); }
    private String nullToDash(TextField tf) { return tf != null && tf.getText() != null && !tf.getText().isBlank() ? tf.getText().trim() : "-"; }
    private String valueStr(ComboBox<String> cb) {
        return cb != null && cb.getValue() != null ? cb.getValue() : "-";
    }
    private String value(ComboBox<StoreOption> cb) {
        return cb != null && cb.getValue() != null ? cb.getValue().name : "-";
    }
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
            if (pickupBranch != null && pickupBranch.getValue() != null) {
                StoreOption so = pickupBranch.getValue();
                pk.setBranchName(so.name);
                try {
                    dto.setStoreId(Long.parseLong(so.id));   // must be a real DB id
                } catch (Exception ex) {
                    error("Pickup", "Selected branch is invalid. Please reselect.");
                    return; // abort confirm
                }
            } else {
                error("Pickup", "Please select a pickup branch.");
                return;
            }

            if (pickupDate != null)   pk.setPickupDate(pickupDate.getValue());
            if (pickupTime != null)   pk.setPickupTime(pickupTime.getText());
            if (pickupPhone != null)  pk.setPhone(pickupPhone.getText());
            dto.setPickup(pk);
        } else {
            DeliveryInfoDTO d = new DeliveryInfoDTO();
            d.setCity(CityBox != null ? CityBox.getText() : null);
            d.setStreet(StreetText != null ? StreetText.getText() : null);
            d.setHouse(HouseText != null ? HouseText.getText() : null);
            // Add these three lines:
            if (DeliveryDatePicker != null && DeliveryDatePicker.getValue() != null) {
                d.setDeliveryDate(DeliveryDatePicker.getValue());
            }
            if (DeliveryTimeText != null) {
                d.setDeliveryTime(DeliveryTimeText.getText());
            }

            if (PhoneText != null) {
                d.setPhone(PhoneText.getText());
            }

            dto.setDelivery(d);

            // Default store for delivery
            if (resolvedDefaultStoreId != null) {
                dto.setStoreId(resolvedDefaultStoreId);
            } else {
                error("Delivery", "Store list not ready yet. Please wait a moment and try again.");
                return;
            }


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
                ok.setHeaderText("Order confirmed");
                ok.showAndWait();
                // Optionally: navigate to Orders view here, if you have a Nav util.
            } else {
                Alert er = new Alert(Alert.AlertType.ERROR, r.getMessage());
                er.setHeaderText("Checkout failed");
                er.showAndWait();
            }
        });
    }

    @Subscribe
    public void onStores(GetStoresResponse r) {
        Platform.runLater(() -> {
            if (r == null || r.stores == null) return;

            storesLoaded = true;                  // <-- mark as ready
            storeOptions.clear();
            storeOptions.addAll(r.stores);

            if (pickupBranch != null) {
                pickupBranch.getItems().setAll(storeOptions);
                if (pickupBranch.getValue() == null && !pickupBranch.getItems().isEmpty()) {
                    pickupBranch.getSelectionModel().selectFirst();
                }
            }

            // choose a safe default for delivery
            if (resolvedDefaultStoreId == null && !storeOptions.isEmpty()) {
                try { resolvedDefaultStoreId = Long.parseLong(storeOptions.get(0).id); } catch (Exception ignored) {}
            }
        });
    }


    // ---------- Step navigation handlers (match FXML onAction) ----------
    @FXML private void handleBack1() { setStep(1); }
    @FXML private void handleNext1() {
        if (validateMethodStep()) {
            setStep(2);
        }
    }
    @FXML private void handleBack2() { setStep(1); }
    @FXML private void handlePay()   {
        if (validatePaymentStep()) {
            setStep(3);
            if (reviewBox != null) {
                reviewBox.setText(buildReviewText());
            }
        }
    }
    @FXML private void handleBack3() { setStep(2); }
    @FXML private void handleConfirm(){ onConfirm(); }

    // ---------- Validation ----------

    private boolean validateMethodStep() {
        // Prevent placeholder IDs (101/102/...) from being used
        if (!storesLoaded) {
            error("Stores", "Store list is still loading. Please wait a second and try again.");
            return false;
        }

        // Minimal identity sanity
        if (isBlank(FullNameText)) { error("Missing name", "Please enter your name."); return false; }
        if (isBlank(EmailText))    { error("Missing email", "Please enter your email."); return false; }

        // Pickup or delivery specific checks
        if (btnPickup != null && btnPickup.isSelected()) {
            if (pickupBranch == null || pickupBranch.getValue() == null) {
                error("Pickup", "Please select a pickup branch."); return false;
            }
            if (pickupDate == null || pickupDate.getValue() == null) {
                error("Pickup", "Please choose a pickup date."); return false;
            }
            if (isBlank(pickupTime))  { error("Pickup", "Please enter a pickup time."); return false; }
            if (isBlank(pickupPhone)) { error("Pickup", "Please enter a phone number for pickup."); return false; }
        } else {
            if (isBlank(CityBox))   { error("Delivery", "City is required."); return false; }
            if (isBlank(StreetText)){ error("Delivery", "Street is required."); return false; }
            if (isBlank(HouseText)) { error("Delivery", "House number is required."); return false; }

            if (giftCheck != null && giftCheck.isSelected()) {
                if (isBlank(RecepientNameText))  { error("Gift", "Recipient name is required."); return false; }
                if (isBlank(RecipientPhoneText)) { error("Gift", "Recipient phone is required."); return false; }
                // Gift note optional by your spec
            }
        }
        return true;
    }

    private boolean validatePaymentStep() {
        // Phone and ID required here
        if (isBlank(PhoneText))     { error("Payment", "Phone number is required."); return false; }
        if (isBlank(IdNumberText))  { error("Payment", "ID number is required."); return false; }

        // Card number: 16 digits
        String card = text(CardNumberText);
        if (!isDigits(card, 16)) { error("Payment", "Card number must be exactly 16 digits."); return false; }

        // Expiration dropdowns must be selected
        if (MMBOX == null || MMBOX.getValue() == null || MMBOX.getValue().isBlank()) {
            error("Payment", "Please select card expiration month."); return false;
        }
        if (YYBOX == null || YYBOX.getValue() == null || YYBOX.getValue().isBlank()) {
            error("Payment", "Please select card expiration year."); return false;
        }

        // CVV: 3 digits
        if (!isDigits(text(cvvText), 3)) { error("Payment", "CVV must be exactly 3 digits."); return false; }

        // Cardholder name recommended but not strictly required; enforce if you want:
        if (isBlank(fullNameText)) { error("Payment", "Cardholder full name is required."); return false; }

        return true;
    }

    private boolean isBlank(TextField tf) {
        return tf == null || tf.getText() == null || tf.getText().trim().isEmpty();
    }

    private boolean isDigits(String s, int exactLen) {
        if (s == null || s.length() != exactLen) return false;
        for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    private void error(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(header);
        a.showAndWait();
    }

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
