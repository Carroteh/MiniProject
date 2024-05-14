package acsse.csc03a3.miniproject.utils;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityUtils {
    public SecurityUtils() {}

    /**
     * Method that verifies a signed input
     * @param signed the signature  to verify
     * @param message the original message
     * @return true of the input is valid, false otherwise
     */
    public static boolean verify(String message, byte[] signed, String publicKey) {
        boolean verified = false;
        PublicKey pubKey = SecurityUtils.stringToPublicKey(publicKey);
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Signature signature = Signature.getInstance("ECDSA", "BC");
            signature.initVerify(pubKey);
            signature.update(message.getBytes());
            verified = signature.verify(signed);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return verified;
    }

    /**
     * Method that encodes an ECPublic key into a string
     * @param publicKey the public key to encode
     * @return the string representation of the public key
     */
    public static String publicKeyToString(PublicKey publicKey){
        try {
            SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
            return Base64.getEncoder().encodeToString(spki.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String privateKeyToString(PrivateKey privateKey){
        byte[] privateKeyBytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(privateKeyBytes);
    }

    /**
     * Function  that decodes a string into a public key
     * @param publicKeyString The string to decode
     * @return The decoded public key
     */
    public static PublicKey stringToPublicKey(String publicKeyString) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
