package main.runner;

import main.runner.facade.ClientFacade;
import main.runner.facade.impl.ClientFacadeImpl;

public class Run {
    static ClientFacade client = new ClientFacadeImpl();

    public static void main(String[] args) {
        client.start();
    }
}
