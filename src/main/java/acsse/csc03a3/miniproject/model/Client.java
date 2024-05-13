package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.AuthenticationPayload;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.RequestPayload;
import acsse.csc03a3.miniproject.utils.NotifListener;
import acsse.csc03a3.miniproject.utils.SecurityUtils;
import webphone.webphone;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Client extends User{
    private String username;
    private String id;
    private webphone wobj;

    public Client(String username) {
        super();
        this.username = username;

        //VOIP Setup
        this.wobj = new webphone();
        wobj.API_SetNotificationListener(new NotifListener());
        wobj.API_SetParameter("serveraddress", "127.0.0.1:5061");
        wobj.API_SetParameter("setserverfromtarget", "2");
        wobj.API_SetParameter("register", "0");
        wobj.API_SetParameter("username", username);
        wobj.API_SetParameter("loglevel", "5");
        wobj.API_SetParameter("signalingport", "3310");

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

    public void accept() {
        wobj.API_Accept();
    }

    public void call(String username) {
        wobj.API_Start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(authenticate(username)) {
            wobj.API_Call(-1, username + "@127.0.0.1:3310");
        }
        else {
            System.err.println("Could not start voice call with " + username);
        }
    }

    public void hangup() {
        wobj.API_Hangup();
    }

    private boolean authenticate(String calleeUsername) {
        sendMessage("AUTH");
        String reply = readMessage();
        boolean authed = false;
        if(reply.startsWith("100")) {
            AuthenticationPayload payload = new AuthenticationPayload(SecurityUtils.publicKeyToString(publicKey), username, calleeUsername);
            Transaction<AuthenticationPayload> transaction = new Transaction<>(id, "Server", payload);
            byte[] signature = this.sign(transaction.toString());
            sendTransaction(id, "Server", payload, signature, transaction.toString());

            reply = readMessage();
            if(reply.startsWith("100")) {
                authed = true;
            } else {
                System.err.println("Authentication failed");
            }
        }
        else {
            System.err.println("Error while authenticating.");
        }
        return authed;
    }

    public List<String> getTrustedList() {
        List<String> trustedList = new ArrayList<String>();
        sendMessage("LIST");
        String reply = readMessage();
        if(reply.startsWith("100")) {
            RequestPayload payload = new RequestPayload(SecurityUtils.publicKeyToString(publicKey), id);
            Transaction<RequestPayload> transaction = new Transaction<>("User", "Server", payload);
            byte[] signature = this.sign(transaction.toString());
            sendTransaction(id, "Server", payload, signature, transaction.toString());
            reply = readMessage();
            if(reply.startsWith("100")) {
                trustedList = (List<String>) readObject();
            }
            else {
                System.err.println("Error retrieving trusted list");
            }
        }
        return trustedList;
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
                this.id = id;
                socket.receive(receivePkt);
                String hash = new String(receivePkt.getData()).trim();
                socket.receive(receivePkt);
                String strTicket = new String(receivePkt.getData()).trim();
                byte[] ticket = Base64.getDecoder().decode(strTicket);

                socket.close();
                return new ClientRegistrationPayload(id,username, hash, ticket, SecurityUtils.publicKeyToString(this.publicKey));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.err.println("An Admin is not registered.");
        }
        return null;
    }

}
