package acsse.csc03a3.miniproject.net.server;

import acsse.csc03a3.Blockchain;
import acsse.csc03a3.miniproject.blockchain.BlockchainManager;
import acsse.csc03a3.miniproject.payloads.Payload;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public static void main(String[] args) {start(3301);}

    /**
     * Function that accepts client connections and delegates them to a client handler
     * @param port the port to listen on
     */
    public static void start(int port) {
        //Create blockchain
        Blockchain<Payload> blockchain = new Blockchain<>();
        BlockchainManager bcManager = new BlockchainManager(blockchain);
        ConcurrentHashMap<String, String> adminDetails = new ConcurrentHashMap<>();
        List<String> trustedUsers = Collections.synchronizedList(new ArrayList<>());

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while(true) {
                System.out.println("Awaiting client on port " + port);
                Socket connection = serverSocket.accept();

                //Create a clientHandler
                ClientHandler clientHandler = new ClientHandler(connection, bcManager, adminDetails, trustedUsers);
                //Create a new thread for the client
                new Thread(clientHandler).start();

                System.out.println("A client has connected: " + connection);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
