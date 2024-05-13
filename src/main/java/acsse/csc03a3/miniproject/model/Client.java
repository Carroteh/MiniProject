package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.net.server.ClientHandler;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Base64;

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

    public void getAdmin() {
        sendMessage("ADMIN");
        ClientHandler admin = (ClientHandler)this.readObject();
        admin.sendMessage("REGREQ", false);

    }

    public ClientRegistrationPayload registerRequest() {

            sendMessage("REGREQ");

            //Get the admin network details from the server
            String reply = readMessage();
            if(reply.startsWith("100")) {
                String adminAddress = reply.substring(4);
                int adminPort = Integer.parseInt(readMessage().substring(4));

                try {
                    //Connect to the admin directly
                    DatagramSocket socket = new DatagramSocket(3303, InetAddress.getByName("localhost"));
                    String message = "TICKETREQ";
                    byte[] msgBuffer = message.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(msgBuffer, msgBuffer.length, InetAddress.getByName(adminAddress), adminPort);
                    socket.send(sendPacket);
                    System.out.println("Sent Ticket request to admin.");
                    byte[] pkBuffer = SecurityUtils.publicKeyToString(this.publicKey).getBytes();
                    sendPacket = new DatagramPacket(pkBuffer, pkBuffer.length, InetAddress.getByName(adminAddress), adminPort);
                    socket.send(sendPacket);

                    byte[] buffer = new byte[1024];
                    DatagramPacket receivePkt = new DatagramPacket(buffer, buffer.length);

                    socket.receive(receivePkt);
                    String id = new String(receivePkt.getData()).trim();
                    socket.receive(receivePkt);
                    String hash = new String(receivePkt.getData()).trim();
                    socket.receive(receivePkt);
                    String strTicket = new String(receivePkt.getData()).trim();
                    byte[] ticket = Base64.getDecoder().decode(strTicket);

                    socket.close();
                    return new ClientRegistrationPayload(id, hash, ticket, SecurityUtils.publicKeyToString(this.publicKey));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                System.err.println("An Admin is not registered.");
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
            readMessage();
        }
    }
}
