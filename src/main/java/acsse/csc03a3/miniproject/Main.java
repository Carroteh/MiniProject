package acsse.csc03a3.miniproject;

import acsse.csc03a3.miniproject.model.Admin;
import acsse.csc03a3.miniproject.model.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Admin admin = new Admin();
        admin.associate();
        Client client = new Client();
        client.associate();

//        List<Transaction<Payload>> listy = new ArrayList<>();
//        listy.add(new ETransaction<>("among", "suse", new AdminAssociationPayload("1234", "WUSHDJLKXCJV")));
//        listy.add((new ETransaction<>("yoda", "gaming", new AdminAssociationPayload("4321", "ALSKJDLKASJD"))));
//        Blockchain<Payload> blockchain = new Blockchain();
//        blockchain.registerStake("among", 1);
//        blockchain.addBlock(listy);
//
//        List<Transaction<Payload>> listy2 = new ArrayList<>();
//        listy2.add(new Transaction<>("among", "suse", new AdminAssociationPayload("1234", "WUSHDJLKXCJV")));
//        listy2.add((new Transaction<>("yoda", "gaming", new AdminAssociationPayload("4321", "ALSKJDLKASJD"))));
//        Blockchain<Payload> blockchain2 = new Blockchain();
//        blockchain2.registerStake("among", 1);
//        blockchain2.addBlock(listy2);
//
//        System.out.println(blockchain);
//        System.out.println(blockchain2);
//
//        BlockchainParser parser = new BlockchainParser(blockchain);
//        ArrayList<Block<Payload>> arr =  parser.getBlocks();
//
//        for(Block<Payload> block : arr){
//            System.out.println(block.getPreviousHash() + " "  + block.getTransactions());
//        }


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