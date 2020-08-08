package main.client.service.impl;

import main.client.service.KeyService;
import main.client.service.PrintService;
import main.client.service.RSAService;

import java.util.Base64;

import static main.client.facade.impl.ClientFacadeImpl.msgInQueue;

public class PrintServiceImpl implements PrintService {
    private RSAService rsaService;
    private KeyService keyService;

    public void setRSAAndKeyServices(RSAService rsaService, KeyService keyService){
        this.rsaService = rsaService;
        this.keyService = keyService;
    }

    public void printInMessages() {
        try {
            if (msgInQueue.size() != 0) {
                String rawMsg = msgInQueue.take();
                if (keyService.getSharedKey() != null) {
                    System.out.println("string: " + rawMsg);

                    rawMsg = rsaService.decryptMessage(Base64.getDecoder().decode(rawMsg));
                }
                System.out.println(rawMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
