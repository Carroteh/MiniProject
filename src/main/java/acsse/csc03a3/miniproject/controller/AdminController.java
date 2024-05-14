package acsse.csc03a3.miniproject.controller;

import acsse.csc03a3.miniproject.model.Admin;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdminController {
    @javafx.fxml.FXML
    private TextField txtAdminPuK;
    @javafx.fxml.FXML
    private TextField txtAdminID;
    @javafx.fxml.FXML
    private TextField txtAdminPrK;
    @javafx.fxml.FXML
    private Button btnRegister;
    @javafx.fxml.FXML
    private Label lblRegisterStatus;
    @javafx.fxml.FXML
    private TextArea txtLog;
    private Admin admin;

    @javafx.fxml.FXML
    public void initialize() {
        admin = new Admin(txtLog, txtAdminID, txtAdminPuK, txtAdminPrK, lblRegisterStatus);
    }


    @javafx.fxml.FXML
    public void onAdminRegister(ActionEvent actionEvent) {
        admin.associate();
    }
}
