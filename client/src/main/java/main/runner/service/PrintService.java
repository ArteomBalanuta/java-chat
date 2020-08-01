package main.runner.service;

public interface PrintService {

    void setRSAAndKeyServices(RSAService rsaService, KeyService keyService);

    void printInMessages();
}
