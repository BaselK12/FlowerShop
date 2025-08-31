package il.cshaifasweng.OCSFMediatorExample.client.employee;

/**
 * Sample Skeleton for 'EmployeeEditor.fxml' Controller Class
 */

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class EmployeeEditorController {

    @FXML // fx:id="ActiveBox"
    private CheckBox ActiveBox; // Value injected by FXMLLoader

    @FXML // fx:id="CancelBtn"
    private Button CancelBtn; // Value injected by FXMLLoader

    @FXML // fx:id="EmailTxt"
    private TextField EmailTxt; // Value injected by FXMLLoader

    @FXML // fx:id="ErrorLabel"
    private Text ErrorLabel; // Value injected by FXMLLoader

    @FXML // fx:id="NameTxt"
    private TextField NameTxt; // Value injected by FXMLLoader

    @FXML // fx:id="PhoneTxt"
    private TextField PhoneTxt; // Value injected by FXMLLoader

    @FXML // fx:id="RoleBox"
    private ComboBox<?> RoleBox; // Value injected by FXMLLoader

    @FXML // fx:id="SalaryTxt"
    private TextField SalaryTxt; // Value injected by FXMLLoader

    @FXML // fx:id="SaveBtn"
    private Button SaveBtn; // Value injected by FXMLLoader

}

