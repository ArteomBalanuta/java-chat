package main.runner.facade.impl;

import main.runner.facade.ClientFacade;
import main.runner.service.*;
import main.runner.service.impl.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.Base64;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.concurrent.Executors.newScheduledThreadPool;

//TODO FIX
public class ClientFacadeImpl implements ClientFacade {
    private final String HOST_ADDRESS = "localhost";
    private final int PORT = 800;

    private final ConnectionService connectionService = new ConnectionServiceServiceImpl();
    private final KeyService keyService = new KeyServiceImpl();
    private final RSAService rsaService = new RSAServiceImpl();
    private final PrintService printService = new PrintServiceImpl();
    private final ReadService readService = new ReadServiceImpl();

    public static final int CAPACITY = 300;

    //TODO FIX public
    public static BlockingQueue<String> msgOutQueue = new ArrayBlockingQueue<>(CAPACITY, false);
    public static BlockingQueue<String> msgInQueue = new ArrayBlockingQueue<>(CAPACITY, false);

    public static final int NUMBER_OF_THREADS = 2;
    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(1);
    private static final ScheduledExecutorService scheduler = newScheduledThreadPool(NUMBER_OF_THREADS);

    void sendMessages() {
        try {
            if (msgOutQueue.size() != 0) {
                String toSend = msgOutQueue.take();
                sendMessage(toSend);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //TODO FIX
    void sendMessage(String msg) {
        connectionService.write(msg);
    }

    private void receiveMessages() {
        String msg = connectionService.read();
        if (msg != null && !msg.trim().isEmpty()) {
            msg = msg.trim();

            if (msg.contains("serverPublicKey ")) {
                int serverPublicKeyOffset = msg.indexOf("serverPublicKey ") + 16;
                String base64ServerPublicKey = msg.substring(serverPublicKeyOffset);
                byte[] serverPublicKey = Base64.getDecoder().decode(base64ServerPublicKey);
                keyService.setServerPublicKey(serverPublicKey);
                keyService.generateSharedKey();
                System.out.println("Shared key has been set!");
                System.out.println("[ClientFacade] Got server public key: " + msg);
                return;
            }

            msgInQueue.add(msg);
        }
    }

    void outStart() {
        new Thread(() -> {
            scheduler.scheduleWithFixedDelay(readService::readUserInput, 0, 10, TimeUnit.MILLISECONDS);
            scheduler.scheduleWithFixedDelay(this::sendMessages, 0, 10, TimeUnit.MILLISECONDS);
        }).start();
    }

    void inStart() {
        new Thread(() -> {
            scheduler.scheduleWithFixedDelay(this::receiveMessages, 0, 10, TimeUnit.MILLISECONDS);
            scheduler.scheduleWithFixedDelay(printService::printInMessages, 0, 10, TimeUnit.MILLISECONDS);
        }).start();
    }

    void setUpServices(){
        connectionService.setEnc(ISO_8859_1);
        connectionService.setConnection(HOST_ADDRESS, PORT);

        KeyPair keys = keyService.generateKeys();
        keyService.saveKeysOnDisk(keys);
        String msgPublicKey = keyService.getClientPublicKeyInBase64ToBeSentToServer();
        rsaService.setKeyService(keyService);
        printService.setRSAAndKeyServices(rsaService, keyService);
        readService.setRSAServiceAndKeyService(rsaService, keyService);
        this.sendMessage(msgPublicKey);
    }

    public void start() {
        setUpServices();
        appExecutor.submit(new Thread(this::outStart));
        appExecutor.submit(new Thread(this::inStart));
    }

}
