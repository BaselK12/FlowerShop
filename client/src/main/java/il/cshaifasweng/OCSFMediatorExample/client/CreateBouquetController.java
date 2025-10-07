package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.TableRow;

import java.net.URL;
import java.util.*;
import java.util.ResourceBundle;

public class CreateBouquetController implements Initializable {

    // ====== Header ======
    @FXML private Label totalPriceLable; // note: label typo preserved from FXML

    // ====== Left Panel ======
    @FXML private TableView<Flower> availableFlowerTable;
    @FXML private TableColumn<Flower, String> colAvailName;
    @FXML private TableColumn<Flower, Double> colAvailPrice;
    @FXML private TableColumn<Flower, Void> volaVailAdd; // "Add" column

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

    // ====== Data ======
    private final List<Flower> availableFlowers = new ArrayList<>();
    private final Map<String, SelectedItem> selectedMap = new LinkedHashMap<>();

    // =======================================================
    // Initialization
    // =======================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAvailableTable();
        setupSelectedTable();
        loadMockData();
        updateAvailableTable();
        updateTotalPrice();
        setupButtonActions();
    }

    // =======================================================
    // Table setup
    // =======================================================
    private void setupAvailableTable() {
        colAvailName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAvailPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        volaVailAdd.setCellFactory(param -> new TableCell<>() {
            private final Button addBtn = new Button("Add");

            {
                addBtn.getStyleClass().addAll("button", "btn-primary");
                addBtn.setOnAction(e -> {
                    Flower f = getTableView().getItems().get(getIndex());
                    addFlowerToBouquet(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(addBtn);
                }
            }
        });

        // Hover highlight via CSS class (no pseudo selectors)
        availableFlowerTable.setRowFactory(tv -> {
            TableRow<Flower> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.getStyleClass().add("table-row-highlight"));
            row.setOnMouseExited(e -> row.getStyleClass().remove("table-row-highlight"));
            return row;
        });
    }

    private void setupSelectedTable() {
        colSellName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSelQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        ColSelSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

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
    private void loadMockData() {
        availableFlowers.add(new Flower("Red Roses", 120));
        availableFlowers.add(new Flower("White Tulips", 95));
        availableFlowers.add(new Flower("Sunflower Joy", 89.9));
        availableFlowers.add(new Flower("Orchid Elegance", 150));
        availableFlowers.add(new Flower("Pink Peonies", 135.5));
    }

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
    private void addFlowerToBouquet(Flower flower) {
        selectedMap.compute(flower.getName(), (k, v) -> {
            if (v == null) return new SelectedItem(flower.getName(), 1, flower.getPrice());
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
    public static class Flower {
        private final String name;
        private final double price;

        public Flower(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
    }

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
}
