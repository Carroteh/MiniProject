package acsse.csc03a3.miniproject.payloads;

public class ClientRegistrationRequestPayload extends Payload{

    private String id;

    public ClientRegistrationRequestPayload(String publicKey){
        super(publicKey);
        payloadId = 2;
    }

    @Override
    public String toString() {
        return payloadId + "`" + publicKey;
    }
}
