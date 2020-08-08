package main.server.engine.server.service.impl;

import main.server.engine.server.service.KeyService;
import main.server.models.user.User;

import javax.crypto.KeyAgreement;
import java.io.BufferedWriter;
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

    public static final String SERVER_PUBLIC_KEY_FILE_NAME = "serverPublicKey";
    public static final String SERVER_PRIVATE_KEY_FILE_NAME = "serverPrivateKey";
    private final String USER_DIR = "user.dir";

    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    private KeyPair keyPair;

    public KeyServiceImpl() {
        generateServerKeys();
    }

    @Override
    public PrivateKey getServerPrivateKey() {
        return serverPrivateKey;
    }

    @Override
    public void setServerPrivateKey(PrivateKey serverPrivateKey) {
        this.serverPrivateKey = serverPrivateKey;
    }

    @Override
    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    @Override
    public void setServerPublicKey(PublicKey serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    @Override
    public KeyPair getKeyPair() {
        return keyPair;
    }

    @Override
    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @Override
    public KeyPair generateServerKeys() {
        KeyPair keyPair = null;
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(DIFFIE_HELLMAN);
            keyPairGenerator.initialize(KEY_SIZE);

            keyPair = keyPairGenerator.generateKeyPair();
            this.keyPair = keyPair;

            serverPrivateKey = keyPair.getPrivate();
            serverPublicKey = keyPair.getPublic();
            String base64PublicKey = new String(Base64.getEncoder().encode(serverPublicKey.getEncoded()));
            String base64PrivateKey = new String(Base64.getEncoder().encode(serverPrivateKey.getEncoded()));

            System.out.println("[KeyService] Generated server public key (b64): " + serverPublicKey.getFormat() + "  \n" + base64PublicKey);
            System.out.println("[KeyService] Generated server private key (b64): " + serverPrivateKey.getFormat() + "  \n" + base64PrivateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    @Override
    public void generateAndSetSharedKeyToUser(User user) {
        try {
            final KeyAgreement keyAgreement = KeyAgreement.getInstance(DIFFIE_HELLMAN);
            keyAgreement.init(serverPrivateKey);
            keyAgreement.doPhase(user.getUserPublicKey(), true);

            user.setSharedKey(shortenSecretKey(keyAgreement.generateSecret()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUserPublicKey(User user, byte[] userPublicKeyBytes) {
        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance(DIFFIE_HELLMAN).generatePublic(new X509EncodedKeySpec(userPublicKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        user.setUserPublicKey(publicKey);
    }

    @Override
    public boolean validatePublicKey(byte[] key) {
        boolean isValid = false;
        try {
            Base64.getDecoder().decode(key);
            isValid = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    @Override
    public void shareServerPublicKeyToUser(User user) {
        try {
            BufferedWriter userWriter = user.getUserWriter();
            userWriter.flush();
            String msg = getServerPublicKeyInBase64ToBeSentToClient();
            userWriter.write(msg + '\n');
            userWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveKeysOnDisk(KeyPair keys) {
        String userDir = System.getProperty(USER_DIR);

        writeKeyToFile(userDir, SERVER_PUBLIC_KEY_FILE_NAME, keys.getPublic());
        writeKeyToFile(userDir, SERVER_PRIVATE_KEY_FILE_NAME, keys.getPrivate());
    }

    private void writeKeyToFile(String path, String fileName, Key key) {
        try {
            Files.write(Path.of(path + "\\" + fileName), key.getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getServerPublicKeyInBase64ToBeSentToClient() {
        String base64PublicKey = new String(Base64.getEncoder().encode(serverPublicKey.getEncoded()));
        return "serverPublicKey " + base64PublicKey;
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
}
