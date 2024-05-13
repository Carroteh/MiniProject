package acsse.csc03a3.miniproject;

import acsse.csc03a3.miniproject.model.Admin;
import acsse.csc03a3.miniproject.model.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        Admin admin = new Admin();
        admin.associate();
        Client client1 = new Client("JOe");
        client1.associate();
        client1.register();
        Client client2 = new Client("mama");
        client2.associate();
        client2.register();
        List<String> list = client1.getTrustedList();
        for(String s : list) {
            System.out.println(s);
        }
        System.out.println("CALLING " + list.get(1));
        Thread.sleep(1000);
        client1.call(list.get(1));
        //client2.accept();
        //client2.hangup();


        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}