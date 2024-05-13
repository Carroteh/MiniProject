package acsse.csc03a3.miniproject.net.server;

import acsse.csc03a3.miniproject.blockchain.BlockchainManager;
import acsse.csc03a3.miniproject.blockchain.ETransaction;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.ClientTicketPayload;
import acsse.csc03a3.miniproject.payloads.Payload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    protected Socket connection;

    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;

    private final BlockchainManager bcManager;
    private ConcurrentHashMap<String, ClientHandler> clients;
    private ClientTicketPayload clientTicket;

    public ClientHandler(Socket connection, BlockchainManager bcManager, ConcurrentHashMap<String, ClientHandler> clients) {
        this.clients = clients;
        this.connection = connection;
        this.bcManager = bcManager;
        this.oos = null;
        this.ois = null;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        try {
            this.oos = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
            oos.flush();
            this.ois = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));

            while(true) {
                String response;

                //Check if client disconnected
                if((response = readMessage()).isEmpty()) {
                    return;
                }

                StringTokenizer tokens = new StringTokenizer(response, " ");
                int numTokens = tokens.countTokens();
                String command = tokens.nextToken();

                if(command.equals("ASSOC") && numTokens == 2) {
                    String type = tokens.nextToken();
                    if(type.equals("Admin")) {
                        handleAdminAssociation();
                    } else if(type.equals("User")) {
                        handleUserAssocation();
                    }
                }
                else if(command.equals("REGREQ") && numTokens == 1) {
                    handleUserRegistrationRequest();
                }
                else if(command.equals("REGISTER") && numTokens == 1) {
                    handleUserRegistration();
                }
                else if(command.equals("TICKET") && numTokens == 1) {
                    this.clientTicket = (ClientTicketPayload) readObject();
                }
                else {
                    sendMessage("Bad request", true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ClientTicketPayload getClientTicket() {
        return this.clientTicket;
    }

    private void handleUserRegistration() {
        sendMessage("100 Continue", false);
        ETransaction<Payload> transaction = receiveTransaction();
        if(transaction != null) {
            ClientRegistrationPayload payload = (ClientRegistrationPayload) transaction.getData();
            //check if user is already registered
            if(bcManager.checkUserRegistered(payload.getId())) {
                sendMessage("User already registered", true);
            }
            else {
                String hash = payload.getHash();
                byte[] ticket = payload.getTicket();
                String adminPK = bcManager.getAdminPK();
                //Verify the clients ticket
                if(SecurityUtils.verify(hash, ticket, adminPK)) {
                    bcManager.addTransaction(transaction);
                    bcManager.registerStake(payload.getPublicKey(), 50);
                    sendMessage("Client successfully registered on the blockchain.", false);
                }
                else {
                    sendMessage("Client ticket verification failed, registration aborted", true);
                }
            }
        }
        else {
            sendMessage("Failed to verify or read transaction.", true);
        }
    }

    private void handleUserAssocation() {
        //Check if an admin is registered
        if(!bcManager.checkAdminExistance()) {
            sendMessage("Admin does not exist.", true);
            return;
        }

        sendMessage("Continue", false);
        ETransaction<Payload> transaction = receiveTransaction();
        if(transaction != null) {
            if(this.bcManager.checkUserAssociation(transaction.getData().getPublicKey())) {
                sendMessage("User already associated.", true);
            }
            else {
                this.bcManager.addTransaction(transaction);
                sendMessage("User successfully associated.", false);
            }
        }
        else {
            sendMessage("Association failed.", true);
        }
    }

    public void handleUserRegistrationRequest() {
        //Check if an admin is registered
        if(!bcManager.checkAdminExistance()) {
            sendMessage("Admin does not exist.", true);
            return;
        }
        String sender = this.readMessage();
        String receiver = this.readMessage();
        Payload data = (Payload)this.readObject();
        byte[] signature = this.readBytes();
        String token = this.readMessage();

        ClientHandler admin = clients.get("admin");

        admin.sendMessage("REGREQ", false);
        admin.sendMessage(sender, false);
        admin.sendMessage(receiver, false);
        admin.sendObject(data);
        admin.sendBytes(signature);
        admin.sendMessage(token, false);
        //Admin sends ticket to its handler
        //Access ticket from admin handler
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ClientTicketPayload clientTicket = admin.getClientTicket();
        sendObject(clientTicket);
    }

//    public void handleUserRegistrationRequest() {
//        //Check if an admin is registered
//        if(!bcManager.checkAdminExistance()) {
//            sendMessage("Admin does not exist.", true);
//            return;
//        }
//
//        sendMessage("Continue", false);
//
//        ClientHandler admin =  clients.get("admin");
//        admin.sendMessage("REGREQ", false);
//        String reply = admin.readMessage();
//        System.out.println("ADMIN REPLY: " + reply);
//        if(reply.startsWith("100")) {
//            //Transfer checks if user has been associated
//            transferTransactionToAdmin();
//            reply = readMessage();
//            if(reply.startsWith("100")) {
//                String id = admin.readMessage();
//                String hash = admin.readMessage();
//                byte[] ticket = admin.readBytes();
//
//                //Send ticket and ID to client
//                sendMessage(id, false);
//                sendMessage(hash, false);
//                sendBytes(ticket);
//            }
//            else {
//                sendMessage("Registration failed", true);
//            }
//        }
//        else {
//            System.err.println("Admin is not responding.");
//        }
//    }

    public void handleAdminAssociation() {
        sendMessage("Continue", false);
        try {
            ETransaction<Payload> transaction =  receiveTransaction();
            if(transaction != null) {
                if(this.bcManager.checkAdminExistance()) {
                    sendMessage("An Admin is already associated", true);
                }
                else {
                    bcManager.addTransaction(transaction);
                    bcManager.registerStake(transaction.getData().getPublicKey(), 50);
                    //Register admin with the server
                    clients.put("admin", this);
                    sendMessage("Admin has successfully been associated", false);
                }
            }
            else {
                sendMessage("Association failed", true);
            }
        }
        catch(ClassCastException e) {
            e.printStackTrace();
        }
    }


    /**
     * Function that sends a plain text message to the client
     * @param message the message being sent
     * @param err if the message is and error or not
     */
    public void sendMessage(String message, boolean err) {
        try {
            if(err) {
                oos.writeUTF("101 " + message);
                oos.flush();
                System.out.println("S: 101 " + message);
            }
            else {
                oos.writeUTF("100 " + message);
                oos.flush();
                System.out.println("S: 100 " + message);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads a plaintext message from the socket
     * @return string of the message
     */
    public String readMessage() {
        String message = "";

        try {
            message = (String)ois.readObject();
            System.out.println("C: " + message);
        }
        catch(SocketException ex) {
            System.out.println("Client has disconnected, terminating handler.");
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Function that reads an array of bytes
     * @return the array of bytes
     */
    public byte[] readBytes() {
        try {
            byte[] arr;
            System.out.println("Reading bytes...");
            int length = ois.readInt();
            arr = new byte[length];
            ois.readFully(arr, 0, length);
            System.out.println("Read bytes");
            return arr;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Function that sends a byte array over the network
     * @param data the data to send
     */
    public void sendBytes(byte[] data) {
        try {
            //Send size
            System.out.println("Sending bytes...");
            oos.writeInt(data.length);
            oos.flush();
            oos.write(data, 0, data.length);
            System.out.println("Sent bytes.");
            oos.flush();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Function that sends an object over the net
     * @param obj the object to net, must be serializable
     */
    public void sendObject(Object obj) {
        try {
            System.out.println("Sending object...");
            oos.writeObject(obj);
            System.out.println("Sent object.");
            oos.flush();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Function that reads an object from the socket
     * @return the object
     */
    public Object readObject() {
        Object obj = null;
        try {
            System.out.println("Reading object...");
            obj = ois.readObject();
            System.out.println("Read object.");
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    /**
     * Function that sends an atomized transaction over the net
     * @param sender the sender
     * @param receiver the receiver
     * @param data the payload
     * @param signature the digital signature
     * @param token the original message corresponding to the signature
     */
    public void sendTransaction(String sender, String receiver, Payload data, byte[] signature, String token) {
        this.sendMessage(sender, false);
        this.sendMessage(receiver, false);
        this.sendObject(data);
        this.sendBytes(signature);
        this.sendMessage(token, false);
    }

    /**
     * Receives and sends the transaction to the admin
     */
    public void transferTransactionToAdmin() {
        String sender = this.readMessage();
        String receiver = this.readMessage();
        Payload data = (Payload)this.readObject();
        byte[] signature = this.readBytes();
        String token = this.readMessage();

        ClientHandler admin =  clients.get("admin");
        if(bcManager.checkUserAssociation(data.getPublicKey())) {
            //admin.sendTransaction(sender, receiver, data, signature, token);
            admin.sendMessage(sender, false);
            admin.sendMessage(receiver, false);
            admin.sendObject(data);
            admin.sendBytes(signature);
            admin.sendMessage(token, false);
        }
        else {
            sendMessage("User has not been associated.", true);
        }
    }

    /**
     * Function that reads and creates a transaction from the net
     * @return the transaction read
     */
    public ETransaction<Payload> receiveTransaction() {
        String sender = this.readMessage();
        String receiver = this.readMessage();
        ETransaction<Payload> transaction = null;
        String pk = "";

        Payload data = (Payload) this.readObject();

        transaction = new ETransaction<>(sender, receiver, data);
        pk = data.getPublicKey();

        byte[] signature = this.readBytes();
        String token = this.readMessage();

        if (SecurityUtils.verify(token, signature, pk)) {
            System.out.println("Successfully verified transaction.");
        } else {
            System.out.println("Failed to verify transaction.");
            return null;
        }

        return transaction;
    }
}
