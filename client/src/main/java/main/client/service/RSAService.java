package main.client.service;

public interface RSAService {

    void setKeyService(KeyService keyService);

    byte[] encryptMessage(final String message);

    String decryptMessage(final byte[] message);
}
