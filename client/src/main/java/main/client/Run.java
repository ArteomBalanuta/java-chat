package main.client;

import main.client.facade.ClientFacade;
import main.client.facade.impl.ClientFacadeImpl;

public class Run {
    static ClientFacade client = new ClientFacadeImpl();

    public static void main(String[] args) {
        client.start();
    }
}
