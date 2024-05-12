package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationRequestPayload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;

public class Client extends User{
    public Client() {
        super();
    }

    @Override
    public void associate() {
        //create association payload
        ClientAssociationPayload payload = new ClientAssociationPayload(SecurityUtils.publicKeyToString(this.publicKey));

        //create a transaction
        Transaction<ClientAssociationPayload> transaction = new Transaction<>("User", "Server", payload);
        //Sign the string representation of the transaction
        byte[] signature = this.sign(transaction.toString());

        //start association
        sendMessage("ASSOC User");
        String response = readMessage();
        if(response.substring(0,3).equals("100")) {
            //Send the transaction
            sendTransaction("User", "Server", payload, signature, transaction.toString());
        }
        else {
            System.err.println("Error initiating association");
        }
        readMessage();
    }

    public ClientRegistrationPayload registerRequest() {
        //Create registration payload
        ClientRegistrationRequestPayload payload = new ClientRegistrationRequestPayload(SecurityUtils.publicKeyToString(this.publicKey));

        //create a transaction
        Transaction<ClientRegistrationRequestPayload> transaction = new Transaction<>("User", "Server", payload);
        //Sign the string representation of the transaction
        byte[] signature = this.sign(transaction.toString());

        //Start registration
        sendMessage("REGREQ");
        String response = readMessage();
        if(response.startsWith("100")) {
            //Send the transaction
            sendTransaction("User", "Server", payload, signature, transaction.toString());
            response = readMessage();
            if(response.startsWith("100")) {
                String id = readMessage();
                String hash = readMessage();
                byte[] ticket = readBytes();
                return new ClientRegistrationPayload(id, hash, ticket, SecurityUtils.publicKeyToString(publicKey));
            }
            else {
                System.err.println("Error retrieving ticket.");
            }
        }
        else {
            System.err.println("Error initiating registration");
        }
        return null;
    }

    public void register() {
        ClientRegistrationPayload registration = registerRequest();
        if(registration != null) {
            Transaction<ClientRegistrationPayload> transaction = new Transaction<>(registration.getId(), "Server", registration);
            byte[] signature = this.sign(transaction.toString());
            sendMessage("REGISTER");
            String reply = readMessage();
            if(reply.startsWith("100")) {
                sendTransaction(registration.getId(), "Server", registration, signature, transaction.toString());
            }
            else {
                System.err.println("Error in registration");
            }
        }
    }
}
