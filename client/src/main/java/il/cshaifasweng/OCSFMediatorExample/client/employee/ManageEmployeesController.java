package il.cshaifasweng.OCSFMediatorExample.client.employee;



import il.cshaifasweng.OCSFMediatorExample.client.App;
import il.cshaifasweng.OCSFMediatorExample.client.ui.Nav;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.*;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;



public class ManageEmployeesController {

    private static volatile String returnToFxml =
            "/il/cshaifasweng/OCSFMediatorExample/client/Admin/AdminDashboard.fxml";
    public static void setReturnTo(String fxml) { returnToFxml = fxml; }

    // Toolbar
    @FXML private CheckBox ActiveOnly;
    @FXML private TextField SearchTxt;
    @FXML private ComboBox<String> RoleFilter;

    // buttons
    @FXML private Button AddBtn;
    @FXML private Button DeleteBtn;
    @FXML private Button EditBTn;
    @FXML private Button RefreshBtn;

    @FXML private Button BackBtn;

    // Table and Columns
    @FXML private TableView<EmployeeVM> TableView;
    @FXML private TableColumn<EmployeeVM, Number> IdCol;        // the id in the company
    @FXML private TableColumn<EmployeeVM, Number> SalaryCol;    // the salary of the employee

    @FXML private TableColumn<EmployeeVM, String> NameCol;
    @FXML private TableColumn<EmployeeVM, String> PhoneCol;
    @FXML private TableColumn<EmployeeVM, String> RoleCol;
    @FXML private TableColumn<EmployeeVM, String> EmailCol;
    @FXML private TableColumn<EmployeeVM, String> GenderCol;
    @FXML private TableColumn<EmployeeVM, String> HireCol;

    @FXML private TableColumn<EmployeeVM, Boolean> ActiveCol;

    // context menu
    @FXML private MenuItem ctxDelete;
    @FXML private MenuItem ctxEdit;

    // status bar
    @FXML private ProgressIndicator Loading;
    @FXML private Label StatusLabel;

    private final ObservableList<EmployeeVM> backing = FXCollections.observableArrayList(
            (EmployeeVM vm) -> new Observable[]{
                    vm.nameProperty(), vm.emailProperty(), vm.phoneProperty(),
                    vm.roleProperty(), vm.activeProperty(), vm.salaryProperty(),
                    vm.hireDateProperty()}
    );


    public void initialize() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        TableView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        });

        // Filters setup
        RoleFilter.setItems(FXCollections.observableArrayList("All", "Manager", "Cashier", "Florist", "Driver"));
        RoleFilter.getSelectionModel().selectFirst();

        // Table cell value factories
        IdCol.setCellValueFactory(c -> c.getValue().idProperty());
        NameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        GenderCol.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> c.getValue().getGender() == null ? "" : prettifyGender(c.getValue().getGender()),
                c.getValue().genderProperty()
        ));
        EmailCol.setCellValueFactory(c -> c.getValue().emailProperty());
        PhoneCol.setCellValueFactory(c -> c.getValue().phoneProperty());
        RoleCol.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> c.getValue().getRole() == null ? "" : prettifyRole(c.getValue().getRole()),
                c.getValue().roleProperty()
        ));
        ActiveCol.setCellValueFactory(c -> c.getValue().activeProperty());
        SalaryCol.setCellValueFactory(c -> c.getValue().salaryProperty());
        HireCol.setCellValueFactory(c ->
                Bindings.createStringBinding(
                        () -> c.getValue().getHireDate() == null ? "" :
                                c.getValue().getHireDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        c.getValue().hireDateProperty()));
        ActiveCol.setCellFactory(CheckBoxTableCell.forTableColumn(ActiveCol));

        // Table data hooks
        TableView.setItems(backing);

        // Selection-dependent buttons
        EditBTn.disableProperty().bind(TableView.getSelectionModel().selectedItemProperty().isNull());
        DeleteBtn.disableProperty().bind(TableView.getSelectionModel().selectedItemProperty().isNull());

        // Context menu actions
        ctxEdit.setOnAction(e -> onEdit());
        ctxDelete.setOnAction(e -> onDelete());

        // Double-click to edit
        TableView.setRowFactory(tv -> {
            TableRow<EmployeeVM> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (!row.isEmpty() && evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2) {
                    onEdit();
                }
            });
            return row;
        });

        // Live filters
        SearchTxt.textProperty().addListener((o, a, b) -> fetchEmployees());
        RoleFilter.valueProperty().addListener((o, a, b) -> fetchEmployees());
        ActiveOnly.selectedProperty().addListener((o, a, b) -> fetchEmployees());

        // Toolbar actions
        AddBtn.setOnAction(e -> onAdd());
        EditBTn.setOnAction(e -> onEdit());
        DeleteBtn.setOnAction(e -> onDelete());
        RefreshBtn.setOnAction(e -> fetchEmployees());
        BackBtn.setOnAction(e -> {
            System.out.println("[RegisterUI] Back clicked -> " + returnToFxml);
            cleanup();
            Nav.go(BackBtn, returnToFxml);
        });

        // Initial load
        Platform.runLater(() -> {fetchEmployees();});

    }

    private void fetchEmployees() {
        setLoading(true);
        StatusLabel.setText("Loading employees...");

        boolean onlyActive = ActiveOnly.isSelected();
        String roleStr = RoleFilter.getValue();
        String search = SearchTxt.getText() != null ? SearchTxt.getText().trim() : "";

        EmployeeRole role = null;
        if (roleStr != null && !"All".equalsIgnoreCase(roleStr)) {
            switch (roleStr) {
                case "Manager" -> role = EmployeeRole.STORE_MANAGER;
                case "Cashier" -> role = EmployeeRole.CASHIER;
                case "Florist" -> role = EmployeeRole.FLORIST;
                case "Driver"  -> role = EmployeeRole.DELIVERY;
            }
        }

        try {
            App.getClient().sendToServer(new GetEmployeesRequest(onlyActive, role, search));
        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
        }
    }

    private void onAdd() {
        openEditor(null);
    }

    private void onEdit() {
        EmployeeVM vm = TableView.getSelectionModel().getSelectedItem();
        if (vm != null) {
            openEditor(vm);
        }
    }


    // helper function to open the window
    private void openEditor(EmployeeVM vm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/il/cshaifasweng/OCSFMediatorExample/client/Employee/EmployeeEditor.fxml"));
            Parent root = loader.load();

            EmployeeEditorController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle(vm == null ? "Add Employee" : "Edit Employee");
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            controller.setEmployee(vm); // null = new, non-null = edit
            stage.show();

            StatusLabel.setText(vm == null
                    ? "Adding new employee…"
                    : "Editing employee '" + vm.getName() + "'");

        } catch (IOException e) {
            e.printStackTrace();
            StatusLabel.setText(vm == null
                    ? "Failed to open Add Employee window"
                    : "Failed to open editor for " + vm.getName());
        } finally {
            setLoading(false);
        }
    }

    private void onDelete() {
        EmployeeVM vm = TableView.getSelectionModel().getSelectedItem();
        if (vm == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete employee '" + vm.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        confirm.initOwner(TableView.getScene().getWindow());
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                setLoading(true);
                StatusLabel.setText("Deleting " + vm.getName() + "…");
                try {
                    App.getClient().sendToServer(new DeleteEmployeeRequest(vm.getId()));
                } catch (Exception e) {
                    setLoading(false);
                    e.printStackTrace();
                }
            }
        });
    }


    private void setLoading(boolean v) {
        Loading.setVisible(v);
        TableView.setDisable(v);
    }

    // ===== EventBus: responses published by SimpleClient =====

    @Subscribe
    public void onEmployeesFetched(GetEmployeesResponse response) {
        System.out.println("[CLIENT] Received Employees: " + response.getEmployees().size());
        Platform.runLater(() -> {
            backing.setAll(response.getEmployees().stream().map(this::toVM).toList());
            setLoading(false);
            StatusLabel.setText("Loaded " + backing.size() + " employees");
        });
    }

    @Subscribe
    public void onEmployeeDeleted(DeleteEmployeeResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                backing.removeIf(vm -> vm.getId() == response.getEmployeeId());
                StatusLabel.setText("Employee deleted");
            } else {
                new Alert(Alert.AlertType.ERROR, response.getMessage(), ButtonType.OK).showAndWait();
                StatusLabel.setText("Delete failed");
            }
            setLoading(false);
        });
    }


    @Subscribe
    public void onEmployeeCreated(CreateEmployeeResponse response) {
        Platform.runLater(() -> {
            var d = response.getEmployee();
            backing.removeIf(vm -> vm.getId() == d.getId());
            backing.add(toVM(d));
            StatusLabel.setText("Employee '" + d.getName() + "' added");
        });
    }


    @Subscribe
    public void onEmployeeUpdated(UpdateEmployeeResponse response) {
        var d = response.getEmployee();

        javafx.application.Platform.runLater(() -> {
            // remove old copy
            backing.removeIf(vm -> vm.getId() == d.getId());
            // add updated one
            backing.add(toVM(d));

            StatusLabel.setText("Employee updated");
        });
    }



    @Subscribe
    public void onEmployeesError(EmployeesEvents.Error ev) {
        setLoading(false);
        new Alert(Alert.AlertType.ERROR, ev.getMessage(), ButtonType.OK).showAndWait();
    }

    private EmployeeVM toVM(EmployeesDTO.Employee d) {
        return new EmployeeVM(
                d.getId(),
                d.getName(),
                d.getGender(),
                d.getEmail(),
                d.getPhone(),
                d.getRole(),
                d.isActive(),
                d.getSalary(),
                d.getHireDate()
        );
    }

    private String prettifyRole(EmployeeRole role) {
        if (role == null) return "";
        return switch (role) {
            case STORE_MANAGER -> "Manager";
            case CASHIER       -> "Cashier";
            case FLORIST       -> "Florist";
            case DELIVERY      -> "Driver";
            case OTHER         -> "Other";
        };
    }

    private String prettifyGender(Gender gender) {
        return switch (gender) {
            case Female -> "Female";
            case Male   -> "Male";
            case Other  -> "Other";
        };
    }

    private void cleanup() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            System.out.println("[RegisterUI] EventBus unregistered");
        }
    }


}

