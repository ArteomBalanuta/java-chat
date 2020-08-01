package main.runner.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyService {

    KeyPair getKeys();

    PublicKey getPublicKey();

    PrivateKey getPrivateKey();

    PublicKey getServerPublicKey();

    byte[] getSharedKey();

    void setKeys(KeyPair keys);

    void generateSharedKey();
    KeyPair generateKeys();

    void setServerPublicKey(byte[] serverPublicKeyBytes);

    void saveKeysOnDisk(KeyPair keys);
    String getClientPublicKeyInBase64ToBeSentToServer();
}
