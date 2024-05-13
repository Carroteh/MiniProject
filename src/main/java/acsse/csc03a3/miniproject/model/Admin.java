package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.blockchain.ETransaction;
import acsse.csc03a3.miniproject.payloads.AdminAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientTicketPayload;
import acsse.csc03a3.miniproject.payloads.Payload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Admin extends User {
    private String ID;
    private boolean listening = true;

    public Admin() {
        super();
        generateID();
        startListening();
    }

    private void startListening() {
        //Read unplanned requests from the server
        listening = true;
        new Thread(() -> {
            String message;
            System.out.println("listening");
            while(listening) {
                message = readMessage();
                if(message.equals("100 REGREQ")) {
                    handleUserRegistrationRequest();
                }
            }
        }).start();
    }

    private void stopListening() {
        System.out.println("stopListening");
        listening = false;
    }

    private void handleUserRegistrationRequest() {
        stopListening();
        System.out.println("handling user registration request");
        ETransaction<Payload> transaction = receiveTransaction();
        System.out.println("RECEIVED USER TRANSACTION");
        System.out.println("TRANSACTION" + transaction);
        String id = generateUserID();
        String hash = bcHash(id + transaction.getData().getPublicKey());
        if(!hash.isEmpty()) {
            //Create ticket for the user to register
            byte[] ticket = this.sign(hash);
            sendMessage("TICKET");
            System.out.println("SENDING TICKET");
            ClientTicketPayload ticketForClient = new ClientTicketPayload(SecurityUtils.publicKeyToString(publicKey), id, hash, ticket);
            sendObject(ticketForClient);
            sendMessage("SENT TICKET");
        }
        else {
            System.err.println("Error creating user ticket hash");
            sendMessage("101 Could not generate user ticket.");
        }
        startListening();
    }

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


    private String generateUserID() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int id = rand.nextInt(5000);
        return String.valueOf(id);
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
        stopListening();
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
        startListening();
    }
}
