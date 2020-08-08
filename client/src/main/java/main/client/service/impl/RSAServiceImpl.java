package main.client.service.impl;

import main.client.service.KeyService;
import main.client.service.RSAService;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RSAServiceImpl implements RSAService {
    private KeyService keyService;

    public void setKeyService(KeyService keyService) {
        this.keyService = keyService;
    }

    public byte[] encryptMessage(final String message) {
        byte[] encryptedMessage = new byte[0];
        try {
            final byte[] sharedKey = keyService.getSharedKey();
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(sharedKey, "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            encryptedMessage = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    public String decryptMessage(final byte[] message) {
        String decryptedMessage = null;
        try {
            final byte[] sharedKey = keyService.getSharedKey();
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(sharedKey, "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            decryptedMessage = new String(cipher.doFinal(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }

}
