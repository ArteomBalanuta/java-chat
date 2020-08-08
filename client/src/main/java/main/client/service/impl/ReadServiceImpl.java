package main.client.service.impl;

import main.client.service.KeyService;
import main.client.service.RSAService;
import main.client.service.ReadService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import static main.client.facade.impl.ClientFacadeImpl.msgOutQueue;

public class ReadServiceImpl implements ReadService {
    private RSAService rsaService;
    private KeyService keyService;

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    //TODO FIX
    public void readUserInput() {
        try {
            String msg = reader.readLine();
            if (msg != null) {
                if (keyService.getSharedKey() != null) {
                    Base64.Encoder encoder = Base64.getEncoder();
                    msg = new String(encoder.encode(rsaService.encryptMessage(msg)));
                }
                msgOutQueue.add(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRSAServiceAndKeyService(RSAService rsaService, KeyService keyService) {
        this.rsaService = rsaService;
        this.keyService = keyService;
    }
}
