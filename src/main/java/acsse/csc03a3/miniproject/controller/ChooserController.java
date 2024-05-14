package acsse.csc03a3.miniproject.controller;

import acsse.csc03a3.miniproject.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ChooserController {

    private Stage stage;
    @javafx.fxml.FXML
    private AnchorPane stump;

    @javafx.fxml.FXML
    public void initialize() {

    }

    @javafx.fxml.FXML
    public void onChooseAdmin(ActionEvent actionEvent) throws IOException {
        this.stage = (Stage) stump.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("admin-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Admin Panel");
        stage.setScene(scene);
        stage.show();
    }

    @javafx.fxml.FXML
    public void onStartServer(ActionEvent actionEvent) throws IOException {
        this.stage = (Stage) stump.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
    }

    @javafx.fxml.FXML
    public void onChooseClient(ActionEvent actionEvent) throws IOException {
        this.stage = (Stage) stump.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Client Panel");
        stage.setScene(scene);
        stage.show();
    }
}
