package acsse.csc03a3.miniproject.payloads;

public class AdminAssociationPayload extends Payload {
    private String adminId;

    public AdminAssociationPayload(String adminId, String adminPublicKey) {
        super(adminPublicKey);
        this.adminId = adminId;
        this.payloadId = 1;
    }

    @Override
    public String toString() {
        return payloadId + "`" + adminId + "`" + publicKey;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }
}
