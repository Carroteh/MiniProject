package acsse.csc03a3.miniproject.blockchain;

import java.io.Serializable;
import java.util.Date;

public class SerializableTransaction<T> implements Serializable {
    private String sender;
    private String receiver;
    private T data;
    private long timestamp;

    public SerializableTransaction(String sender, String receiver, T data) {
        this.sender = sender;
        this.receiver = receiver;
        this.data = data;
        this.timestamp = (new Date()).getTime();
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String s) {
        this.sender = s;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public void setReceiver(String r) {
        this.receiver = r;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T d) {
        this.data = d;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String toString() {
        String var10000 = this.sender;
        return "Transaction{sender='" + var10000 + "', receiver='" + this.receiver + "', data=" + String.valueOf(this.data) + ", timestamp=" + this.timestamp + "}";
    }
}