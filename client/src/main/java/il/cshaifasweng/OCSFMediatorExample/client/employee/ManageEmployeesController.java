package il.cshaifasweng.OCSFMediatorExample.client.employee;



import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import javafx.beans.Observable;
import org.greenrobot.eventbus.Subscribe;
import il.cshaifasweng.OCSFMediatorExample.client.SimpleClient;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import org.greenrobot.eventbus.EventBus;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Predicate;

public class ManageEmployeesController {

    // Toolbar
    @FXML private CheckBox ActiveOnly;
    @FXML private TextField SearchTxt;
    @FXML private ComboBox<String> RoleFilter;

    // buttons
    @FXML private Button AddBtn;
    @FXML private Button DeleteBtn;
    @FXML private Button EditBTn;
    @FXML private Button RefreshBtn;

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
    private FilteredList<EmployeeVM> filtered;
    private SortedList<EmployeeVM> sorted;

    // Mock service for now (no server)
    private final MockEmployeesService service = new MockEmployeesService();

    public void initialize() {

        // subscribe
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
        GenderCol.setCellValueFactory(c -> c.getValue().genderProperty());
        EmailCol.setCellValueFactory(c -> c.getValue().emailProperty());
        PhoneCol.setCellValueFactory(c -> c.getValue().phoneProperty());
        RoleCol.setCellValueFactory(c -> c.getValue().roleProperty());
        ActiveCol.setCellValueFactory(c -> c.getValue().activeProperty());
        SalaryCol.setCellValueFactory(c -> c.getValue().salaryProperty());
        HireCol.setCellValueFactory(c ->
                Bindings.createStringBinding(
                        () -> c.getValue().getHireDate() == null ? "" :
                                c.getValue().getHireDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        c.getValue().hireDateProperty()));
        ActiveCol.setCellFactory(CheckBoxTableCell.forTableColumn(ActiveCol));

        // Table data hooks
        filtered = new FilteredList<>(backing, vm -> true);
        sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(TableView.comparatorProperty());
        TableView.setItems(sorted);

        // Selection-dependent buttons
        EditBTn.disableProperty().bind(TableView.getSelectionModel().selectedItemProperty().isNull());
        DeleteBtn.disableProperty().bind(TableView.getSelectionModel().selectedItemProperty().isNull());

        // Context menu actions
        ctxEdit.setOnAction(e -> onEdit());
        ctxDelete.setOnAction(e -> onDelete());

        // Double-click to edit (placeholder for now)
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
        SearchTxt.textProperty().addListener((o, a, b) -> applyFilters());
        RoleFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        ActiveOnly.selectedProperty().addListener((o, a, b) -> applyFilters());

        // Toolbar actions
        AddBtn.setOnAction(e -> onAdd());
        EditBTn.setOnAction(e -> onEdit());
        DeleteBtn.setOnAction(e -> onDelete());
        RefreshBtn.setOnAction(e -> fetchEmployees());

        // Initial load
        fetchEmployees();
    }

    private void fetchEmployees() {
        setLoading(true);
        StatusLabel.setText("Loading employees...");
        try {
            // replace GetEmployeesMessage with your actual message class
            // we need to send a message to get all the employees
            SimpleClient.getClient().sendToServer("FETCH_EMPLOYEES");
        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
        }
    }

    private void onAdd() {
        setLoading(true);
        StatusLabel.setText("Opening new employee editor…");
        try {
            // simple string command (matches your FETCH/DELETE style)
            SimpleClient.getClient().sendToServer("EMPLOYEES_OPEN_EDITOR:NEW");
        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
        }
    }

    private void onEdit() {
        EmployeeVM vm = TableView.getSelectionModel().getSelectedItem();
        if (vm == null) return;

        // spinner is optional here; opening is fast
        setLoading(true);
        StatusLabel.setText("Opening editor for " + vm.getName() + "…");
        try {
            // Ask server to open editor for this employee
            // (server will reply to THIS client only)
            SimpleClient.getClient().sendToServer("EMPLOYEES_OPEN_EDITOR:EDIT:" + vm.getId());
        } catch (Exception e) {
            setLoading(false);
            e.printStackTrace();
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
                    // Send delete command to server (id appended after colon)
                    SimpleClient.getClient().sendToServer("EMPLOYEES_DELETE:" + vm.getId());
                } catch (Exception e) {
                    setLoading(false);
                    e.printStackTrace();
                }
            }
        });
    }

    private void applyFilters() {
        final String q = SearchTxt.getText() == null ? "" : SearchTxt.getText().trim().toLowerCase(Locale.ROOT);
        final String role = RoleFilter.getValue();
        final boolean onlyActive = ActiveOnly.isSelected();

        Predicate<EmployeeVM> p = vm -> {
            boolean matchQ = q.isEmpty()
                    || vm.getName().toLowerCase(Locale.ROOT).contains(q)
                    || vm.getEmail().toLowerCase(Locale.ROOT).contains(q)
                    || vm.getPhone().toLowerCase(Locale.ROOT).contains(q);
            boolean matchRole = (role == null || role.equals("All")) || role.equals(vm.getRole());
            boolean matchActive = !onlyActive || vm.isActive();
            return matchQ && matchRole && matchActive;
        };
        filtered.setPredicate(p);
        StatusLabel.setText(String.format("Showing %d of %d", TableView.getItems().size(), backing.size()));
    }



    private void setLoading(boolean v) {
        Loading.setVisible(v);
        TableView.setDisable(v);
    }

    // ===== EventBus: responses published by SimpleClient =====

    @Subscribe
    public void onEmployeesFetched(EmployeesEvents.EmployeesFetched ev) {
        // Map DTOs -> VMs and replace the table data
        backing.setAll(ev.getEmployees().stream().map(this::toVM).toList());
        applyFilters();
        setLoading(false);
        StatusLabel.setText("Loaded " + backing.size() + " employees");
    }

    @Subscribe
    public void onEmployeeDeleted(EmployeesEvents.EmployeeDeleted ev) {
        backing.removeIf(vm -> vm.getId() == ev.getEmployeeId());
        applyFilters();
        setLoading(false);                     // <- stop spinner here
        StatusLabel.setText("Employee deleted");
    }

    @Subscribe
    public void onEmployeeCreated(EmployeesEvents.EmployeeCreated ev) {
        var d = ev.getEmployee();
        backing.removeIf(vm -> vm.getId() == d.getId()); // avoid dup if server re-sent
        backing.add(toVM(d));
        applyFilters();
        StatusLabel.setText("Employee added");
    }

    @Subscribe
    public void onEmployeeUpdated(EmployeesEvents.EmployeeUpdated ev) {
        var d = ev.getEmployee();
        for (int i = 0; i < backing.size(); i++) {
            if (backing.get(i).getId() == d.getId()) {
                backing.set(i, toVM(d));
                break;
            }
        }
        applyFilters();
        StatusLabel.setText("Employee updated");
    }



    @Subscribe
    public void onEmployeesError(EmployeesEvents.Error ev) {
        setLoading(false);
        new Alert(Alert.AlertType.ERROR, ev.getMessage(), ButtonType.OK).showAndWait();
    }

    private EmployeeVM toVM(EmployeesDTO.Employee d) {
        return new EmployeeVM(
                d.getId(), d.getName(), d.getGender(), d.getEmail(),
                d.getPhone(), d.getRole(), d.isActive(), d.getSalary(), d.getHireDate()
        );
    }



}

