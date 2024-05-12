package acsse.csc03a3.miniproject.net.server;

import acsse.csc03a3.Blockchain;
import acsse.csc03a3.miniproject.blockchain.BlockchainManager;
import acsse.csc03a3.miniproject.payloads.Payload;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ClientHandler admin;

    public static void main(String[] args) {start(3301);}

    /**
     * Function that accepts client connections and delegates them to a client handler
     * @param port the port to listen on
     */
    public static void start(int port) {
        //Create blockchain
        Blockchain<Payload> blockchain = new Blockchain<>();
        BlockchainManager bcManager = new BlockchainManager(blockchain);

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while(true) {
                System.out.println("Awaiting client on port " + port);
                Socket connection = serverSocket.accept();

                //Create a clientHandler
                ClientHandler clientHandler = new ClientHandler(connection, bcManager, admin);
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
