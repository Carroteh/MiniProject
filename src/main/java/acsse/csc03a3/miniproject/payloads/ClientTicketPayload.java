package acsse.csc03a3.miniproject.payloads;

public class ClientTicketPayload extends Payload{

    private String id;
    private String hash;
    private byte[] ticket;

    public ClientTicketPayload(String pk, String id, String hash, byte[] ticket){
        super(pk);
        this.id = id;
        this.hash = hash;
        this.ticket = ticket;
        this.payloadId = 5;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getTicket() {
        return ticket;
    }

    public void setTicket(byte[] ticket) {
        this.ticket = ticket;
    }
}
