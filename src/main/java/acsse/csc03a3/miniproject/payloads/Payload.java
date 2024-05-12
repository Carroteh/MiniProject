package acsse.csc03a3.miniproject.payloads;

import java.io.Serializable;

public abstract class Payload implements Serializable {
    protected int payloadId;
    protected String publicKey;

    public Payload(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String adminPublicKey) {
        this.publicKey = adminPublicKey;
    }
}
