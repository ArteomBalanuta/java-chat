package main.server.engine.server.service;

import main.server.models.user.User;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyService {

    KeyPair getKeyPair();

    void setKeyPair(KeyPair keys);

    PublicKey getServerPublicKey();

    PrivateKey getServerPrivateKey();

    void setServerPrivateKey(PrivateKey serverPrivateKey);

    void setServerPublicKey(PublicKey serverPublicKey);

    void setUserPublicKey(User user, byte[] userPublicKeyBytes);

    KeyPair generateServerKeys();

    void saveKeysOnDisk(KeyPair keys);

    void shareServerPublicKeyToUser(User user);

    void generateAndSetSharedKeyToUser(User user);

    boolean validatePublicKey(byte[] key);
}
