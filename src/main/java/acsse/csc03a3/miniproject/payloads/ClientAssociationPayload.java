package acsse.csc03a3.miniproject.payloads;

public class ClientAssociationPayload extends Payload {

    public ClientAssociationPayload(String publicKey) {
        super(publicKey);
        this.payloadId = 3;
    }

    @Override
    public String toString() {
        return payloadId + "`" + publicKey;
    }
}
