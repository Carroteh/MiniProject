package acsse.csc03a3.miniproject.payloads;

public class ClientRegistrationPayload extends Payload{

    private String id;

    //Original string of the ticket
    private String hash;
    private byte[] ticket;

    public ClientRegistrationPayload(String id, String hash, byte[] ticket, String publicKey) {
        super(publicKey);
        this.payloadId = 4;
        this.id = id;
        this.hash = hash;
        this.ticket = ticket;
    }
    public String getId() {
        return id;
    }
    public String getHash() {
        return hash;
    }
    public byte[] getTicket() {
        return ticket;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTicket(byte[] ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return payloadId + "`" + id + "`" + hash + "`" + ticket;
    }
}
