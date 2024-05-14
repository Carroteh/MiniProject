package acsse.csc03a3.miniproject.controller;

import acsse.csc03a3.miniproject.model.Client;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

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
    public void initialize() {
        client = new Client(txtLog, txtID, txtPublicKey, txtPrivateKey, null);
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
}
