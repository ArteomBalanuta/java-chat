package main.client.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyService {

    KeyPair getKeys();

    void setKeys(KeyPair keys);

    PublicKey getPublicKey();

    PrivateKey getPrivateKey();

    PublicKey getServerPublicKey();

    void setServerPublicKey(byte[] serverPublicKeyBytes);

    byte[] getSharedKey();

    void generateSharedKey();

    KeyPair generateKeys();

    void saveKeysOnDisk(KeyPair keys);

    String getClientPublicKeyInBase64ToBeSentToServer();
}
