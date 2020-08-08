package main.client.service.impl;

import main.client.service.KeyService;

import javax.crypto.KeyAgreement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyServiceImpl implements KeyService {
    private static final String DIFFIE_HELLMAN = "DH";
    private static final int KEY_SIZE = 1024;
    private final String USER_DIR = "user.dir";

    private KeyPair keyPair;

    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    private PublicKey serverPublicKey;
    private byte[] sharedKey;

    public KeyPair getKeys() {
        return keyPair;
    }

    public PublicKey getPublicKey() {
        return clientPublicKey;
    }

    public PrivateKey getPrivateKey() {
        return clientPrivateKey;
    }

    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(byte[] serverPublicKeyBytes) {
        try {
            this.serverPublicKey = KeyFactory.getInstance(DIFFIE_HELLMAN).generatePublic(new X509EncodedKeySpec(serverPublicKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] getSharedKey() {
        return sharedKey;
    }

    public void setKeys(KeyPair keys) {
        this.keyPair = keys;
    }

    public void setSharedKey(byte[] sharedKeyBytes) {
        this.sharedKey = sharedKeyBytes;
    }

    public void generateSharedKey() {
        try {
            final KeyAgreement keyAgreement = KeyAgreement.getInstance(DIFFIE_HELLMAN);
            keyAgreement.init(this.clientPrivateKey);
            keyAgreement.doPhase(this.serverPublicKey, true);
            this.sharedKey = shortenSecretKey(keyAgreement.generateSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public KeyPair generateKeys() {
        KeyPair keyPair = null;
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(DIFFIE_HELLMAN);
            keyPairGenerator.initialize(KEY_SIZE);

            keyPair = keyPairGenerator.generateKeyPair();

            clientPrivateKey = keyPair.getPrivate();
            clientPublicKey = keyPair.getPublic();
            String base64PublicKey = new String(Base64.getEncoder().encode(clientPublicKey.getEncoded()));
            String base64PrivateKey = new String(Base64.getEncoder().encode(clientPrivateKey.getEncoded()));

            System.out.println("[KeyService] Generated client public key (b64): " + clientPublicKey.getFormat() + "  \n" + base64PublicKey);
            System.out.println("[KeyService] Generated client private key (b64): " + clientPrivateKey.getFormat() + "  \n" + base64PrivateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    //TODO should be send after key generation
    public String getClientPublicKeyInBase64ToBeSentToServer() {
        String base64PublicKey = new String(Base64.getEncoder().encode(clientPublicKey.getEncoded()));
        return "publicKey " + base64PublicKey;
    }

    private byte[] shortenSecretKey(final byte[] longKey) {
        try {
            // Use 8 bytes (64 bits) for DES, 6 bytes (48 bits) for Blowfish
            final byte[] shortenedKey = new byte[8];

            System.arraycopy(longKey, 0, shortenedKey, 0, shortenedKey.length);

            return shortenedKey;

            // Below lines can be more secure
            // final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // final DESKeySpec       desSpec    = new DESKeySpec(longKey);
            //
            // return keyFactory.generateSecret(desSpec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveKeysOnDisk(KeyPair keys) {
        String userDir = System.getProperty(USER_DIR);

        writeKeyToFile(userDir,"publicKey", keys.getPublic());
        writeKeyToFile(userDir,"privateKey", keys.getPrivate());
    }

    private void writeKeyToFile(String path, String fileName,  Key key) {
        try {
            Files.write(Path.of(path + "\\" + fileName), key.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
