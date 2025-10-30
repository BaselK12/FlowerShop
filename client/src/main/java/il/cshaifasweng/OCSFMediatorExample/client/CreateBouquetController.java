package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.FlowerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersResponse;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.TableRow;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;


public class CreateBouquetController {

    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/Catalog/CatalogView.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }
    private final BooleanProperty loggingIn = new SimpleBooleanProperty(false);


    // ====== Header ======
    @FXML private Label totalPriceLable;

    // ====== Left Panel ======
    @FXML private TableView<FlowerDTO> availableFlowerTable;
    @FXML private TableColumn<FlowerDTO, String> colAvailName;
    @FXML private TableColumn<FlowerDTO, Double> colAvailPrice;
    @FXML private TableColumn<FlowerDTO, Void> volaVailAdd;

    // ====== Center Panel ======
    @FXML private FlowPane bouquetPreviewPane;

    // ====== Right Panel ======
    @FXML private TableView<SelectedItem> selectedFlowerTable;
    @FXML private TableColumn<SelectedItem, String> colSellName;
    @FXML private TableColumn<SelectedItem, Integer> colSelQty;
    @FXML private TableColumn<SelectedItem, Double> ColSelSubtotal;
    @FXML private TableColumn<SelectedItem, Void> colSelRemove;

    // ====== Footer Buttons ======
    @FXML private Button btnClear;
    @FXML private Button btnSave;
    @FXML private Button btnCheckout;
    @FXML private Button BackBtn;

    // ====== Data ======
    private final List<FlowerDTO> availableFlowers = new ArrayList<>();
    private final Map<String, SelectedItem> selectedMap = new LinkedHashMap<>();

    // =======================================================
    // Initialization
    // =======================================================
    @FXML
    public void initialize() {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        setupAvailableTable();
        setupSelectedTable();
        setupButtonActions();

        // Ask the server for single-stem flowers
        try {
            App.getClient().sendToServer(new GetFlowersRequest(true));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not load flowers from server.");
        }

        BackBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Back clicked -> " + returnToFxml);
            cleanup();
            Nav.go(BackBtn, returnToFxml);
        });

        updateTotalPrice();
    }

    // =======================================================
    // Table setup
    // =======================================================
    private void setupAvailableTable() {
        colAvailName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAvailPrice.setCellValueFactory(new PropertyValueFactory<>("effectivePrice"));

        volaVailAdd.setCellFactory(param -> new TableCell<>() {
            private final Button addBtn = new Button("Add");

            {
                addBtn.getStyleClass().addAll("button", "btn-primary");
                addBtn.setOnAction(e -> {
                    FlowerDTO f = getTableView().getItems().get(getIndex());
                    addFlowerToBouquet(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addBtn);
            }
        });

        availableFlowerTable.setRowFactory(tv -> {
            TableRow<FlowerDTO> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.getStyleClass().add("table-row-highlight"));
            row.setOnMouseExited(e -> row.getStyleClass().remove("table-row-highlight"));
            return row;
        });
    }

    private void setupSelectedTable() {
        colSellName.setCellValueFactory(new PropertyValueFactory<>("name"));
        ColSelSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Replace this line:
        // colSelQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        // With this custom cell factory:
        colSelQty.setCellFactory(param -> new TableCell<>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Label qtyLabel = new Label();
            private final HBox box = new HBox(5, btnMinus, qtyLabel, btnPlus);

            {
                box.setStyle("-fx-alignment: center;"); // center contents
                btnMinus.getStyleClass().add("qty-btn");
                btnPlus.getStyleClass().add("qty-btn");
                qtyLabel.getStyleClass().add("qty-label");
                box.getStyleClass().add("qty-box");

                btnMinus.setOnAction(e -> changeQuantity(-1));
                btnPlus.setOnAction(e -> changeQuantity(+1));
            }

            private void changeQuantity(int delta) {
                SelectedItem item = getTableView().getItems().get(getIndex());
                int newQty = Math.max(0, item.getQuantity() + delta);

                if (newQty == 0) {
                    selectedMap.remove(item.getName());
                } else {
                    item.setQuantity(newQty);
                }

                qtyLabel.setText(String.valueOf(newQty));
                updateSelectedTable();
                updateTotalPrice();
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    SelectedItem current = getTableView().getItems().get(getIndex());
                    qtyLabel.setText(String.valueOf(current.getQuantity()));
                    setGraphic(box);
                }
            }
        });

        // --- Remove column ---
        colSelRemove.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.getStyleClass().addAll("button", "btn-secondary");
                removeBtn.setOnAction(e -> {
                    SelectedItem sel = getTableView().getItems().get(getIndex());
                    removeFromBouquet(sel.getName());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        selectedFlowerTable.setRowFactory(tv -> {
            TableRow<SelectedItem> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.getStyleClass().add("table-row-highlight"));
            row.setOnMouseExited(e -> row.getStyleClass().remove("table-row-highlight"));
            return row;
        });
    }

    // =======================================================
    // Data
    // =======================================================
    private void updateAvailableTable() {
        availableFlowerTable.getItems().setAll(availableFlowers);
    }

    private void updateSelectedTable() {
        selectedFlowerTable.getItems().setAll(selectedMap.values());
    }

    private void updateTotalPrice() {
        double total = selectedMap.values().stream()
                .mapToDouble(SelectedItem::getSubtotal)
                .sum();
        totalPriceLable.setText(String.format("Total: ₪%.2f", total));
    }

    // =======================================================
    // Bouquet logic
    // =======================================================
    private void addFlowerToBouquet(FlowerDTO flower) {
        selectedMap.compute(flower.getName(), (k, v) -> {
            if (v == null) return new SelectedItem(flower.getName(), 1, flower.getEffectivePrice());
            v.setQuantity(v.getQuantity() + 1);
            return v;
        });
        updateSelectedTable();
        updateTotalPrice();
    }

    private void removeFromBouquet(String name) {
        selectedMap.remove(name);
        updateSelectedTable();
        updateTotalPrice();
    }

    private void clearBouquet() {
        selectedMap.clear();
        updateSelectedTable();
        updateTotalPrice();
    }

    // =======================================================
    // Buttons
    // =======================================================
    private void setupButtonActions() {
        btnClear.setOnAction(e -> clearBouquet());

        btnSave.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bouquet Saved");
            alert.setHeaderText(null);
            alert.setContentText("Bouquet saved successfully!");
            alert.showAndWait();
        });

        btnCheckout.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Checkout");
            alert.setHeaderText(null);
            alert.setContentText("Proceeding to checkout...");
            alert.showAndWait();
        });
    }

    // =======================================================
    // Model classes (inner static)
    // =======================================================
    public static class SelectedItem {
        private final String name;
        private int quantity;
        private final double price;

        public SelectedItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getSubtotal() { return price * quantity; }
    }

    // =======================================================
    // Events handler
    // =======================================================
    @Subscribe
    public void onGetFlowersResponse(GetFlowersResponse response) {
        Platform.runLater(() -> {
            availableFlowers.clear();
            availableFlowers.addAll(response.getFlowers());
            updateAvailableTable();
            updateTotalPrice();
        });
    }

    // =======================================================
    // Utility
    // =======================================================
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void cleanup() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            System.out.println("[RegisterUI] EventBus unregistered");
        }
    }
}
