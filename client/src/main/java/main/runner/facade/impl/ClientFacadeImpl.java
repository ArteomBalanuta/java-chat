package main.runner.facade.impl;

import main.runner.facade.ClientFacade;
import main.runner.service.ConnectionService;
import main.runner.service.KeyService;
import main.runner.service.RSAService;
import main.runner.service.impl.ConnectionServiceServiceImpl;
import main.runner.service.impl.KeyServiceImpl;
import main.runner.service.impl.RSAServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.Base64;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ClientFacadeImpl implements ClientFacade {
    private final String HOST_ADDRESS = "localhost";
    private final int PORT = 800;

    private final ConnectionService connectionService = new ConnectionServiceServiceImpl();
    private final KeyService keyService = new KeyServiceImpl();
    private final RSAService rsaService = new RSAServiceImpl();

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static final int CAPACITY = 300;
    private static BlockingQueue<String> msgOutQueue = new ArrayBlockingQueue<>(CAPACITY, false);
    private static BlockingQueue<String> msgInQueue = new ArrayBlockingQueue<>(CAPACITY, false);
    public static final int NUMBER_OF_THREADS = 2;
    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(1);
    private static final ScheduledExecutorService scheduler = newScheduledThreadPool(NUMBER_OF_THREADS);

    //TODO FIX
    void readFromInput() {
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
            }

            msgInQueue.add(msg);
        }
    }

    private void printInMessages() {
        try {
            if (msgInQueue.size() != 0) {
                String rawMsg = msgInQueue.take();
                if (keyService.getSharedKey() != null) {
                    System.out.println("string: " + rawMsg);

                    rawMsg = rsaService.decryptMessage(Base64.getDecoder().decode(rawMsg));
                    System.out.println("true");
                }
                System.out.println(rawMsg);
            }
        } catch (Exception e) {
            System.out.println("false");
            e.printStackTrace();
        }
    }

    void outStart() {
        new Thread(() -> {
            scheduler.scheduleWithFixedDelay(this::readFromInput, 0, 10, TimeUnit.MILLISECONDS);
            scheduler.scheduleWithFixedDelay(this::sendMessages, 0, 10, TimeUnit.MILLISECONDS);
        }).start();
    }

    void inStart() {
        new Thread(() -> {
            scheduler.scheduleWithFixedDelay(this::receiveMessages, 0, 10, TimeUnit.MILLISECONDS);
            scheduler.scheduleWithFixedDelay(this::printInMessages, 0, 10, TimeUnit.MILLISECONDS);
        }).start();
    }

    void setUpServices(){
        connectionService.setEnc(ISO_8859_1);
        connectionService.setConnection(HOST_ADDRESS, PORT);

        KeyPair keys = keyService.generateKeys();
        keyService.saveKeysOnDisk(keys);
        String msgPublicKey = keyService.getClientPublicKeyInBase64ToBeSentToServer();
        rsaService.setKeyService(keyService);
        this.sendMessage(msgPublicKey);
    }

    public void start() {
        setUpServices();
        appExecutor.submit(new Thread(this::outStart));
        appExecutor.submit(new Thread(this::inStart));
    }

}
