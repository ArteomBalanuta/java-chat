package main.runner.service;

public interface ReadService {
    void setRSAServiceAndKeyService(RSAService rsaService, KeyService keyService);
    void readUserInput();
}
