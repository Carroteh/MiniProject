package acsse.csc03a3.miniproject.payloads;

public class RequestPayload extends Payload{
    private String id;
    public RequestPayload(String publickey, String id){
        super(publickey);
        this.payloadId = 5;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return payloadId + "`" + id + "`" + publicKey;
    }
}
