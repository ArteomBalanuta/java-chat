package main.engine.server.service;

import main.models.user.User;

public interface RSAService {

    void setKeyService(KeyService keyService);

    byte[] encryptMessage(User user, final String message);

    String decryptMessage(User user, final byte[] message);
}
