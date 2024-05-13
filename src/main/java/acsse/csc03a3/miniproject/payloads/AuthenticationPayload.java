package acsse.csc03a3.miniproject.payloads;

public class AuthenticationPayload extends Payload {
    private String callerID;
    private String calleeID;

    public AuthenticationPayload(String pk, String callerID, String calleeID) {
        super(pk);
        this.payloadId = 6;
        this.callerID = callerID;
        this.calleeID = calleeID;
    }

    public String getCallerID() {
        return callerID;
    }
    public String getCalleeID() {
        return calleeID;
    }

    @Override
    public String toString() {
        return payloadId + "`" + publicKey +  "`" + callerID + "`" + calleeID;
    }
}
