package main.engine.server.service.impl;

import main.engine.server.service.KeyService;
import main.engine.server.service.RSAService;
import main.models.user.User;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAServiceImpl implements RSAService {
    private KeyService keyService;

    @Override
    public byte[] encryptMessage(User user, final String message) {
        byte[] encryptedMessage = new byte[0];
        try {
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(user.getSharedKey(), "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            encryptedMessage = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    @Override
    public String decryptMessage(User user, final byte[] message) {
        String decryptedMessage = null;
        try {
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(user.getSharedKey(), "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            decryptedMessage = new String(cipher.doFinal(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }

    @Override
    public void setKeyService(KeyService keyService) {
        this.keyService = keyService;
    }
}
