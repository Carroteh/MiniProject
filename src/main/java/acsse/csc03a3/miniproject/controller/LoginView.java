package acsse.csc03a3.miniproject.controller;

import acsse.csc03a3.miniproject.model.Client;
import javafx.event.ActionEvent;

public class LoginView
{
    Client client1;

    @javafx.fxml.FXML
    public void initialize() {
//        Admin admin = new Admin(null);
//        admin.associate();

    }

    @javafx.fxml.FXML
    public void btnCall(ActionEvent actionEvent) {
        client1.authenticate("mama");
        client1.call("mama");
    }

    @javafx.fxml.FXML
    public void btnClient1(ActionEvent actionEvent) {
//        client1 = new Client("JOe");
//        client1.associate();
//        client1.register();
    }

    @javafx.fxml.FXML
    public void btnClient2(ActionEvent actionEvent) {
//        Client client2 = new Client("mama");
//        client2.associate();
//        client2.register();
    }
}