package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.AdminAssociationPayload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Admin extends User {
    private String ID;
    private DatagramSocket socket;


    public Admin(TextArea txtLog, TextField txtID, TextField txtPublicKey, TextField txtPrivateKey, TextField txtClientsRegistered) {
        super(txtLog, txtID, txtPublicKey, txtPrivateKey, txtClientsRegistered);
        generateID();
        startUDPServer();
    }

    public void startUDPServer() {
        try {
            socket = new DatagramSocket(3302);

            new Thread(() -> {
                while (true) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        socket.receive(receivePacket);

                        //Get return details
                        InetAddress address = receivePacket.getAddress();
                        int port = receivePacket.getPort();

                        String message = new String(receivePacket.getData()).trim();

                        if (message.equals("TICKETREQ")) {
                            socket.receive(receivePacket);
                            String pk = new String(receivePacket.getData()).trim();
                            String id = generateUserID(pk);
                            String hash = bcHash(id + pk);

                            if(!hash.isEmpty()) {
                                byte[] ticket = this.sign(hash);
                                sendUDPMessage(id.getBytes(), id.getBytes().length, address, port);
                                sendUDPMessage(hash.getBytes(), hash.getBytes().length, address, port);

                                String strTicket = Base64.getEncoder().encodeToString(ticket);
                                sendUDPMessage(strTicket.getBytes(), strTicket.getBytes().length, address, port);
                                System.out.println("Sent ticket to user.");
                            }
                            else {
                                System.err.println("Could not generate user ticket.");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//    private void handleUserRegistrationRequest() {
//
//        System.out.println("handling user registration request");
//        sendMessage("100 Continue");
//        ETransaction<Payload> transaction = receiveTransaction();
//        String id = generateUserID();
//        String hash = bcHash(id + transaction.getData().getPublicKey());
//        if(!hash.isEmpty()) {
//            //Create ticket for the user to register
//            byte[] ticket = this.sign(hash);
//            sendMessage("100");
//            sendMessage(id);
//            sendMessage(hash);
//            sendBytes(ticket);
//        }
//        else {
//            System.err.println("Error creating user ticket hash");
//            sendMessage("101 Could not generate user ticket.");
//        }
//    }

    private String bcHash(String input) {
        String sha256hex = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            sha256hex = new String(Hex.encode(hash));
            return sha256hex;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha256hex;
    }


    private String generateUserID(String PK) {
        return PK.substring(PK.length()-5, PK.length()-1);
    }

    /**
     * Generate an admin ID based on the first 5 digits on their public key
     */
    private void generateID() {
        String pkey = SecurityUtils.publicKeyToString(this.publicKey);
        this.ID = pkey.substring(pkey.length()-5, pkey.length()-1);
    }

    /**
     * Associate the admin with the blockchain
     */
    @Override
    public void associate() {
        //create association payload
        AdminAssociationPayload payload = new AdminAssociationPayload(ID, SecurityUtils.publicKeyToString(this.publicKey));

        //create a transaction
        Transaction<AdminAssociationPayload> transaction = new Transaction<>(ID, "Server", payload);
        //Sign the string representation of the transaction
        byte[] signature = this.sign(transaction.toString());

        //start association
        sendMessage("ASSOC Admin");
        String response = readMessage();
        if(response.startsWith("100")) {
            //Send the transaction
            sendTransaction(ID, "Server", payload, signature, transaction.toString());
        }
        else {
            System.err.println("Error initiating association");
        }
        readMessage();
    }

    /**
     * Function that sends text to the UDP client
     * @param message the message to send in byte array
     * @param length the length of the byte array
     * @param address the address of the client
     * @param port the port of the client
     */
    private void sendUDPMessage(byte[] message, int length, InetAddress address, int port) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(message, length, address, port);
            socket.send(sendPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
