package acsse.csc03a3.miniproject.blockchain;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class SerializableBlock<T> implements Serializable {
    private String previousHash;
    private List<SerializableTransaction<T>> transactions;
    private String hash;
    private long nonce;

    public SerializableBlock(String previousHash, List<SerializableTransaction<T>> transactions) {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.hash = this.calculateHash();
    }

    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String var10000 = this.previousHash;
            String dataToHash = var10000 + this.transactions.toString() + this.nonce;
            byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuffer = new StringBuilder();
            byte[] var8 = hashBytes;
            int var7 = hashBytes.length;

            for(int var6 = 0; var6 < var7; ++var6) {
                byte b = var8[var6];
                stringBuffer.append(String.format("%02x", b));
            }

            return stringBuffer.toString();
        } catch (Exception var9) {
            Exception e = var9;
            throw new RuntimeException("Could not calculate hash", e);
        }
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    public List<SerializableTransaction<T>> getTransactions() {
        return this.transactions;
    }

    public String getHash() {
        return this.hash;
    }

    public long getNonce() {
        return this.nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
        this.hash = this.calculateHash();
    }

    public String toString() {
        String var10000 = this.previousHash;
        return "Block{previousHash='" + var10000 + "', transactions=" + String.valueOf(this.transactions) + ", hash='" + this.hash + "', nonce=" + this.nonce + "}";
    }
}