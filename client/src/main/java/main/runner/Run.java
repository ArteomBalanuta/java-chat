package main.runner;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class Run {
    public static final String HOST_ADDRESS = "localhost";
    public static final int PORT = 800;
    ConnectionImpl connection = new ConnectionImpl(HOST_ADDRESS, PORT);

    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    private PublicKey serverPublicKey;
    private byte[] sharedKey;

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static final int CAPACITY = 300;
    private static volatile BlockingQueue<String> msgOutQueue = new ArrayBlockingQueue<>(CAPACITY, false);
    private static volatile BlockingQueue<String> msgInQueue = new ArrayBlockingQueue<>(CAPACITY, false);

    public static final int NUMBER_OF_THREADS = 2;
    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(1);
    private static final ScheduledExecutorService outScheduler = newScheduledThreadPool(NUMBER_OF_THREADS);
    private static final ScheduledExecutorService inScheduler = newScheduledThreadPool(NUMBER_OF_THREADS);

    //TODO FIX
    void readFromInput() {
        try {
            String msg = reader.readLine();
            if (msg != null) {
                if (sharedKey != null) {
                    Base64.Encoder encoder = Base64.getEncoder();
                    msg = new String(encoder.encode(encryptMessage(msg)));
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
        connection.write(msg);
    }

    private void receiveMessages() {
        String msg = connection.read();
        if (msg != null && !msg.trim().isEmpty()) {
            msg = msg.trim();

            if (msg.contains("serverPublicKey ")) {
                int serverPublicKeyOffset = msg.indexOf("serverPublicKey ") + 16;
                String base64ServerPublicKey = msg.substring(serverPublicKeyOffset);
                byte[] serverPublicKey = Base64.getDecoder().decode(base64ServerPublicKey);
                setServerPublicKey(serverPublicKey);
                generateSharedKey();
                System.out.println("Shared key has been set!");
            }

            msgInQueue.add(msg);
        }
    }

    private void setServerPublicKey(byte[] serverPublicKeyBytes) {
        try {
            this.serverPublicKey = KeyFactory.getInstance("DH").generatePublic(new X509EncodedKeySpec(serverPublicKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void printInMessages() {
        try {
            if (msgInQueue.size() != 0) {
                String rawMsg = msgInQueue.take();
                if(sharedKey != null){
                    System.out.println("string: " + rawMsg);
                    rawMsg = decryptMessage(Base64.getDecoder().decode(rawMsg));
                    System.out.println("true");
                }
                System.out.println(rawMsg);
            }
        } catch (Exception e) {
            System.out.println("false");
            e.printStackTrace();
        }
    }


    public byte[] encryptMessage(final String message) {
        byte[] encryptedMessage = new byte[0];
        try {
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(sharedKey, "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            encryptedMessage = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    public void generateSharedKey() {
        try {
            final KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(clientPrivateKey);
            keyAgreement.doPhase(serverPublicKey, true);

            sharedKey = shortenSecretKey(keyAgreement.generateSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateKeys() {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(1024);

            final KeyPair keyPair = keyPairGenerator.generateKeyPair();

            clientPrivateKey = keyPair.getPrivate();
            clientPublicKey = keyPair.getPublic();
            String base64PublicKey = new String(Base64.getEncoder().encode(clientPublicKey.getEncoded()));
            System.out.println("client public key: " + clientPublicKey.getFormat() + "  \n" + base64PublicKey);

            sendMessage("publicKey " + base64PublicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String decryptMessage(final byte[] message) {
        String decryptedMessage = null;
        try {
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(sharedKey, "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            decryptedMessage = new String(cipher.doFinal(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }

    private byte[] shortenSecretKey(final byte[] longKey) {
        try {
            // Use 8 bytes (64 bits) for DES, 6 bytes (48 bits) for Blowfish
            final byte[] shortenedKey = new byte[8];

            System.arraycopy(longKey, 0, shortenedKey, 0, shortenedKey.length);

            return shortenedKey;

            // Below lines can be more secure
            // final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // final DESKeySpec       desSpec    = new DESKeySpec(longKey);
            //
            // return keyFactory.generateSecret(desSpec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void outStart() {
        new Thread(() -> {
            inScheduler.scheduleWithFixedDelay(this::readFromInput, 0, 10, TimeUnit.MILLISECONDS);
            inScheduler.scheduleWithFixedDelay(this::sendMessages, 0, 10, TimeUnit.MILLISECONDS);
        }).start();
    }

    void inStart() {
        new Thread(() -> {
            outScheduler.scheduleWithFixedDelay(this::receiveMessages, 0, 10, TimeUnit.MILLISECONDS);
            outScheduler.scheduleWithFixedDelay(this::printInMessages, 0, 10, TimeUnit.MILLISECONDS);
        }).start();
    }

    public static void main(String[] args) {
        Run application = new Run();
        application.generateKeys();

        appExecutor.submit(new Thread(application::outStart));
        appExecutor.submit(new Thread(application::inStart));
    }
}
