package il.cshaifasweng.OCSFMediatorExample.client;



import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ManageEmployeesController {

    @FXML // fx:id="ActiveCol"
    private TableColumn<?, ?> ActiveCol; // Value injected by FXMLLoader

    @FXML // fx:id="ActiveOnly"
    private CheckBox ActiveOnly; // Value injected by FXMLLoader

    @FXML // fx:id="AddBtn"
    private Button AddBtn; // Value injected by FXMLLoader

    @FXML // fx:id="DeleteBtn"
    private Button DeleteBtn; // Value injected by FXMLLoader

    @FXML // fx:id="EditBTn"
    private Button EditBTn; // Value injected by FXMLLoader

    @FXML // fx:id="EmailCol"
    private TableColumn<?, ?> EmailCol; // Value injected by FXMLLoader

    @FXML // fx:id="IdCol"
    private TableColumn<?, ?> IdCol; // Value injected by FXMLLoader

    @FXML // fx:id="Loading"
    private ProgressIndicator Loading; // Value injected by FXMLLoader

    @FXML // fx:id="NameCol"
    private TableColumn<?, ?> NameCol; // Value injected by FXMLLoader

    @FXML // fx:id="PhoneCol"
    private TableColumn<?, ?> PhoneCol; // Value injected by FXMLLoader

    @FXML // fx:id="RoleCol"
    private TableColumn<?, ?> RoleCol; // Value injected by FXMLLoader

    @FXML // fx:id="RoleFilter"
    private ComboBox<?> RoleFilter; // Value injected by FXMLLoader

    @FXML // fx:id="SalaryCol"
    private TableColumn<?, ?> SalaryCol; // Value injected by FXMLLoader

    @FXML // fx:id="SearchTxt"
    private TextField SearchTxt; // Value injected by FXMLLoader

    @FXML // fx:id="StatusLabel"
    private Label StatusLabel; // Value injected by FXMLLoader

    @FXML // fx:id="TableView"
    private TableView<?> TableView; // Value injected by FXMLLoader

    @FXML // fx:id="ctxDelete"
    private MenuItem ctxDelete; // Value injected by FXMLLoader

    @FXML // fx:id="ctxEdit"
    private MenuItem ctxEdit; // Value injected by FXMLLoader

}

