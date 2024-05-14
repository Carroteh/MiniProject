package acsse.csc03a3.miniproject.net.server;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.blockchain.BlockchainManager;
import acsse.csc03a3.miniproject.blockchain.ETransaction;
import acsse.csc03a3.miniproject.payloads.AuthenticationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.Payload;
import acsse.csc03a3.miniproject.payloads.RequestPayload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    protected Socket connection;

    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;

    private final BlockchainManager bcManager;
    private final ConcurrentHashMap<String, String> adminDetails;
    private final List<String> trustedList;
    private boolean running;
    private final TextArea txtLog;

    public ClientHandler(Socket connection, BlockchainManager bcManager, ConcurrentHashMap<String, String> adminDetails, List<String> trustedList, TextArea txtLog) {
        this.trustedList = trustedList;
        this.adminDetails = adminDetails;
        this.connection = connection;
        this.bcManager = bcManager;
        this.txtLog = txtLog;
        this.oos = null;
        this.ois = null;
        this.running = true;
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

            while(running) {
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
                else if(command.equals("LIST") && numTokens == 1) {
                    handleGetTrustedList();
                }
                else if(command.equals("AUTH") && numTokens == 1) {
                    handleMutualAuthentication();
                    return;
                }
                else {
                    sendMessage("Bad request", true);
                }
            }
        } catch (IOException e) {
            Log("IO Exception: " + e.getMessage());
        }
    }

    private void handleMutualAuthentication() {
        sendMessage("Continue", false);
        Transaction<Payload> transaction = receiveTransaction();
        if(transaction != null) {
            AuthenticationPayload payload = (AuthenticationPayload) transaction.getData();
            String callerUsername = payload.getCallerID();
            String calleeUsername = payload.getCalleeID();
            if(bcManager.checkUsernameRegistered(callerUsername) && bcManager.checkUsernameRegistered(calleeUsername)) {
                sendMessage("Successfully authenticated.", false);
            }
            else {
                sendMessage("Authentication failed.", true);
            }
        }
        else {
            sendMessage("Error receiving transaction", true);
        }
    }

    private void handleGetTrustedList() {
        sendMessage("Continue", false);
        Transaction<Payload> transaction = receiveTransaction();
        if(transaction != null) {
            RequestPayload payload = (RequestPayload) transaction.getData();
            if(bcManager.checkUserRegistered(payload.getId())) {
                sendMessage("Validated, sending list.", false);
                bcManager.addTransaction(transaction);
                List<String> arrTrustedList = trustedList.stream().toList();
                sendObject(arrTrustedList);
            }
            else {
                sendMessage("User is not registered", true);
            }
        }
        else {
            sendMessage("Error receiving transaction.", true);
        }
    }

    private synchronized void handleUserRegistration() {
        sendMessage("Continue", false);
        ETransaction<Payload> transaction = receiveTransaction();
        if(transaction != null) {
            ClientRegistrationPayload payload = (ClientRegistrationPayload) transaction.getData();

            //check if user is already registered
            if(bcManager.checkUserRegistered(payload.getId())) {
                sendMessage("User already registered", true);
            }
            else {
                if(bcManager.checkUserAssociation(payload.getPublicKey())) {
                    String hash = payload.getHash();
                    byte[] ticket = payload.getTicket();
                    String adminPK = bcManager.getAdminPK();
                    //Verify the clients ticket
                    if(SecurityUtils.verify(hash, ticket, adminPK)) {
                        bcManager.addTransaction(transaction);
                        bcManager.registerStake(payload.getPublicKey(), 50);

                        //Add new user to the trusted list
                        this.trustedList.add(payload.getUsername());
                        sendMessage("Client successfully registered on the blockchain.", false);
                    }
                    else {
                        sendMessage("Client ticket verification failed, registration aborted", true);
                    }
                }
                else {
                    sendMessage("Client registration failed, Client is not associated with the blockchain, registration aborted", true);
                }
            }
        }
        else {
            sendMessage("Failed to verify or read transaction.", true);
        }
    }

    private synchronized void handleUserAssocation() {
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

    private synchronized void handleUserRegistrationRequest() {
        if(bcManager.checkAdminExistance()) {
            sendMessage(adminDetails.get("address"), false);
            sendMessage("3302", false);
        }
        else {
            sendMessage("Admin does not exist.", true);
        }

    }

    private synchronized void handleAdminAssociation() {
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
                    adminDetails.put("address", this.connection.getInetAddress().getHostName());
                    adminDetails.put("port", this.connection.getPort() + "");
                    sendMessage("Admin has successfully been associated", false);
                }
            }
            else {
                sendMessage("Association failed", true);
            }
        }
        catch(ClassCastException e) {
            Log("ClassCastException: " + e.getMessage());
        }
    }

    private void Log(String message) {
        this.txtLog.appendText(message + "\n");
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
                Log("S: 101 " + message);
            }
            else {
                oos.writeUTF("100 " + message);
                oos.flush();
                Log("S: 100 " + message);
            }
        }
        catch (IOException ex) {
            Log("IOException: " + ex.getMessage());
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
            Log("C: " + message);
        }
        catch(SocketException | EOFException ex) {
            Log("Client has disconnected, terminating handler.");
            try {
                running = false;
                connection.close();
            } catch (IOException e) {
                Log("IOException: " + e.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            Log("IOException: " + e.getMessage());
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
            Log("Reading bytes...");
            int length = ois.readInt();
            arr = new byte[length];
            ois.readFully(arr, 0, length);
            Log("Read bytes: " + length);
            return arr;
        }
        catch(Exception ex) {
            Log("Exception: " + ex.getMessage());
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
            Log("Sending bytes...");
            oos.writeInt(data.length);
            oos.flush();
            oos.write(data, 0, data.length);
            Log("Sent bytes...");
            oos.flush();

        } catch (IOException ex) {
            Log("IOException: " + ex.getMessage());
        }
    }

    /**
     * Function that sends an object over the net
     * @param obj the object to net, must be serializable
     */
    public void sendObject(Object obj) {
        try {
            Log("Sending object...");
            oos.writeObject(obj);
            Log("Sent object...");
            oos.flush();
        }
        catch(Exception ex) {
            Log("IOException: " + ex.getMessage());
        }
    }

    /**
     * Function that reads an object from the socket
     * @return the object
     */
    public Object readObject() {
        Object obj = null;
        try {
            Log("Reading object...");
            obj = ois.readObject();
            Log("Read object...");
        }
        catch(Exception ex) {
            Log("IOException: " + ex.getMessage());
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
            Log("Successfully verified transaction signature.");
        } else {
            Log("Failed to verify transaction signature.");
            return null;
        }

        return transaction;
    }
}
