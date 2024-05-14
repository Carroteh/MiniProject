package acsse.csc03a3.miniproject.model;

import acsse.csc03a3.miniproject.blockchain.ETransaction;
import acsse.csc03a3.miniproject.payloads.Payload;
import acsse.csc03a3.miniproject.utils.SecurityUtils;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

public abstract class User {
    protected ECPublicKey publicKey;
    protected ECPrivateKey privateKey;
    protected Signature signature;

    protected Socket connection;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;
    protected int port;

    protected TextArea txtLog;
    protected TextField txtID;
    protected TextField txtPublicKey;
    protected TextField txtPrivateKey;

    public User(TextArea txtLog, TextField txtID, TextField txtPublicKey, TextField txtPrivateKey) {
        this.txtID = txtID;
        this.txtPublicKey = txtPublicKey;
        this.txtPrivateKey = txtPrivateKey;
        this.txtLog = txtLog;
        generateKeys();
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

    public void associate() {}

    /**
     * Function that generates a public and private key pair using the ECDSA algorithm
     */
    protected void generateKeys() {
        try {
            ECNamedCurveParameterSpec ecspec = ECNamedCurveTable.getParameterSpec("prime192v1");
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");

            g.initialize(ecspec, new SecureRandom());
            KeyPair pair = g.generateKeyPair();

            this.publicKey = (ECPublicKey)pair.getPublic();
            this.privateKey = (ECPrivateKey)pair.getPrivate();
        }
        catch (Exception e) {
            Log("Generate Keys Exception: " + e.getMessage());
        }
    }

    protected void Log(String text) {
        txtLog.appendText(text + "\n");
    }

    /**
     * Function that signs a message using a private key
     * @param input the message to sign
     * @return the signed input
     */
    public byte[] sign(String input) {
        byte[] byteInput = input.getBytes();
        try {
            this.signature = Signature.getInstance("ECDSA", "BC");
            //init the sign and update data to be signed
            signature.initSign(privateKey);
            signature.update(byteInput);

            //sign the data using private key
            return signature.sign();

        } catch (Exception e) {
            Log("Sign Exception: " + e.getMessage());
        }
        return null;
    }

    public void sendMessage(String message) {
        try {
            oos.writeObject(message);
            Log("C: " + message);
            oos.flush();
        }
        catch (IOException ex) {
            Log("IO Exception: " + ex.getMessage());
        }

    }

    public String readMessage() {
        String message = "";
        try {
            message = ois.readUTF();
            Log("S: " + message);
        }
        catch(IOException e) {
            Log("IO Exception: " + e.getMessage());
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
            Log("IO Exception: " + ex.getMessage());
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
            Log("Sent bytes.");
            oos.flush();

        } catch (IOException ex) {
            Log("IO Exception: " + ex.getMessage());
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
            Log("Sent object.");
            oos.flush();
        }
        catch(Exception ex) {
            Log("IO Exception: " + ex.getMessage());
        }
    }

    /**
     * Function that reads an object from the socket
     * @return the object
     */
    public Object readObject() {
        Object obj = null;
        try {
            obj = ois.readObject();
        }
        catch(Exception ex) {
            Log("IO Exception: " + ex.getMessage());
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
    public <T> void sendTransaction(String sender, String receiver, T data, byte[] signature, String token) {
        this.sendMessage(sender);
        this.sendMessage(receiver);
        this.sendObject(data);
        this.sendBytes(signature);
        this.sendMessage(token);
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

        if(SecurityUtils.verify(token, signature, pk)) {
            Log("Successfully verified transaction.");
        }
        else {
            Log("Failed to verify transaction.");
            return null;
        }

        return transaction;
    }
}
