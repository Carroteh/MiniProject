package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.Transaction;
import acsse.csc03a3.miniproject.payloads.AuthenticationPayload;
import acsse.csc03a3.miniproject.payloads.ClientAssociationPayload;
import acsse.csc03a3.miniproject.payloads.ClientRegistrationPayload;
import acsse.csc03a3.miniproject.payloads.RequestPayload;
import acsse.csc03a3.miniproject.utils.NotifListener;
import acsse.csc03a3.miniproject.utils.SecurityUtils;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import webphone.webphone;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Client extends User{
    private String username;
    private String id;
    private webphone wobj;
    private boolean authed;
    private TextField txtUsername;
    private Label lblAssocStatus;
    private Label lblRegStatus;
    private TextField txtAuthed;

    public Client(TextArea txtLog, TextField txtID, TextField txtPublicKey, TextField txtPrivateKey, TextField txtUsername, Label lblAssocStatus, Label lblRegStatus, TextField txtAuthed) {
        super(txtLog, txtID, txtPublicKey, txtPrivateKey);
        this.txtUsername = txtUsername;
        this.txtAuthed = txtAuthed;
        this.lblAssocStatus = lblAssocStatus;
        this.lblRegStatus = lblRegStatus;
        this.txtPublicKey.setText(SecurityUtils.publicKeyToString(publicKey));
        this.txtPrivateKey.setText(SecurityUtils.privateKeyToString(privateKey));
        this.authed = false;
        setupVoIP();
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
        if(response.startsWith("100")) {
            //Send the transaction
            sendTransaction("User", "Server", payload, signature, transaction.toString());
        }
        else {
            Log("Error in initiating association");
            lblAssocStatus.setText("Error in initiating association");
        }
        response = readMessage();
        if(response.startsWith("100")) {
            lblAssocStatus.setText("Successfully associated!");
        }
        else {
            lblAssocStatus.setText("Association failed!");
        }
    }

    public void accept() {
        wobj.API_Accept();
    }

    public void setupVoIP() {
        //VOIP Setup
        this.wobj = new webphone();

        wobj.API_SetNotificationListener(new NotifListener(txtLog));
        wobj.API_SetParameter("serveraddress", "127.0.0.1:1234");
        wobj.API_SetParameter("setserverfromtarget", "2");
        wobj.API_SetParameter("register", "0");
        wobj.API_SetParameter("username", username);
        wobj.API_SetParameter("loglevel", "1");
        //wobj.API_SetParameter("signalingport", "1234");
        wobj.API_Start();
    }


    public void call(String username) {
        setupVoIP();
        authenticate(username);
        if(authed) {
            wobj.API_Call(-1, username + "@127.0.0.1:1234");
        }
        else {
            Log("User not authenticated.");
        }
    }

    public void hangup() {
        wobj.API_Hangup();
        reconnect();
    }

    public void authenticate(String calleeUsername) {
        sendMessage("AUTH");
        String reply = readMessage();
        if(reply.startsWith("100")) {
            AuthenticationPayload payload = new AuthenticationPayload(SecurityUtils.publicKeyToString(publicKey), username, calleeUsername);
            Transaction<AuthenticationPayload> transaction = new Transaction<>(id, "Server", payload);
            byte[] signature = this.sign(transaction.toString());
            sendTransaction(id, "Server", payload, signature, transaction.toString());

            reply = readMessage();
            if(reply.startsWith("100")) {
                this.authed = true;
                txtAuthed.setText("True.");
                disconnect();
            } else {
                txtAuthed.setText("False.");
                Log("Authentication failed");
            }
        }
        else {
            txtAuthed.setText("False.");
            Log("Error while authenticating.");
        }
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
                Log("Error retrieving trusted list");
            }
        }
        return trustedList;
    }

    public void register(String username) {
        this.username = username;
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
                Log("Error in registration");
                lblAssocStatus.setText("Error in registration");
            }
            reply = readMessage();
            if(reply.startsWith("100")) {
                lblRegStatus.setText("Successfully registered!");
                this.id = registration.getId();
                txtID.setText(id);
            }
            else {
                lblRegStatus.setText("Registration failed!");
            }
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
                Log("Sent Ticket request to admin.");

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
                Log("Exception: " + e.getMessage());
            }
        }
        else {
            Log("An Admin is not registered.");
        }
        return null;
    }

    public void disconnect() {
        try {
            this.oos.close();
            this.ois.close();
            this.connection.close();
            Log("Disconnect from server");
        } catch (Exception e) {
            Log("Exception: " + e.getMessage());
        }

    }

    public void reconnect() {
        this.port = 3301;
        try {
            this.connection = new Socket("localhost", port);
            this.oos = new ObjectOutputStream(new BufferedOutputStream(connection.getOutputStream()));
            oos.flush();
            this.ois = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));

        } catch (IOException e) {
            Log("IO Exception: " + e.getMessage());
        }
    }

}
