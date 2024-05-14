package acsse.csc03a3.miniproject.controller;

import acsse.csc03a3.miniproject.model.Client;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class ClientController {
    @javafx.fxml.FXML
    private TextField txtID;
    @javafx.fxml.FXML
    private TextField txtUsername;
    @javafx.fxml.FXML
    private Label lblAssocStatus;
    @javafx.fxml.FXML
    private TextArea txtLog;
    @javafx.fxml.FXML
    private TextField txtPrivateKey;
    @javafx.fxml.FXML
    private ListView lstUsers;
    @javafx.fxml.FXML
    private TextField txtPublicKey;
    @javafx.fxml.FXML
    private AnchorPane ancUserDetails;
    @javafx.fxml.FXML
    private Label lblRegisterStatus;

    private Client client;
    @javafx.fxml.FXML
    private TextField txtContactUsername;
    @javafx.fxml.FXML
    private TextField txtAuthStatus;
    @javafx.fxml.FXML
    private Button btnHangUp;
    @javafx.fxml.FXML
    private Button btnCall;

    @javafx.fxml.FXML
    public void initialize() {
        client = new Client(txtLog, txtID, txtPublicKey, txtPrivateKey, txtUsername, lblAssocStatus, lblRegisterStatus, txtAuthStatus);
    }


    @javafx.fxml.FXML
    public void onRegister(ActionEvent actionEvent) {
        String username = txtUsername.getText();
        client.register(username);
    }

    @javafx.fxml.FXML
    public void onAssociate(ActionEvent actionEvent) {
        client.associate();
    }

    @javafx.fxml.FXML
    public void btnRefreshContacts(ActionEvent actionEvent) {
        getContacts();
    }

    public void getContacts() {
        List<String> contacts =  client.getTrustedList();
        lstUsers.getItems().clear();
        lstUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int index = lstUsers.getSelectionModel().getSelectedIndex();
            String user = contacts.get(index);
            txtContactUsername.setText(user);
            btnCall.setOnAction(e -> {
                client.call(user);
            });
            btnHangUp.setOnAction(e -> {
                client.hangup();
            });
        });
        if(!contacts.isEmpty()) {
            lstUsers.getItems().addAll(contacts);
        }
    }

}
