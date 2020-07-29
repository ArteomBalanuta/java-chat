package main.engine.server;

import main.engine.console.models.GuiMessage;
import main.models.dto.LinkBucketGuiMessage;
import main.models.message.UserMessage;
import main.models.user.User;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static main.utils.Constants.MESSAGE_USER_LEFT;
import static main.utils.Utils.isNotNullOrEmpty;


public class ChatEngine implements Chat {
    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;

    private static final int USERS_MAX_NUMBER = 50;
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final int THREAD_NUMBER = 2;

    private static ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);
    ;
    private static final BlockingQueue<UserMessage> messageQueueBuffer = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, false);
    private static final BlockingQueue<User> userQueue = new ArrayBlockingQueue<>(USERS_MAX_NUMBER, true);

    public final LinkBucketGuiMessage linkBucketGuiMessages;
    private boolean isRunning = false;

    public ChatEngine(LinkBucketGuiMessage linkBucketGuiMessage) {
        linkBucketGuiMessages = linkBucketGuiMessage;
        this.generateKeys();
    }

    public static void saveUser(User user) {
        try {
            userQueue.put(user);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getUserMessages() {
        userQueue.forEach(this::getUserMessage);
    }

    public void shareUserMessages() {
        try {
            if (messageQueueBuffer.size() != 0) {
                final UserMessage userMessage = messageQueueBuffer.take();
                for (User user : userQueue) {
                    sendMessagesTo(user, userMessage);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean validatePublicKey(byte[] key) {
        boolean isValid = false;
        try {
            Base64.getDecoder().decode(key);
            isValid = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    //TODO FIX
    private void getUserMessage(User user) {
        try {
            BufferedReader reader = user.getUserReader();
            if (reader.ready()) {
                String msgBody = reader.readLine().trim();
                if (isNotNullOrEmpty(msgBody)) {
                    UserMessage userMessage = new UserMessage(user.getTrip(), msgBody);
                    if (msgBody.contains("publicKey ")) {
                        int keyOffset = msgBody.indexOf("publicKey ") + 10;
                        byte[] key = msgBody.substring(keyOffset).getBytes();
                        if(validatePublicKey(key)){
                            setUserPublicKey(user, Base64.getDecoder().decode(key));
                            userMessage = new UserMessage(user.getTrip(), "exchanged public key!");
                            shareServerPublicKey(user);
                            generateSharedKey(user);
                        } else {
                            userMessage = new UserMessage(user.getTrip(), "key exchange failed!");
                        }
                        messageQueueBuffer.put(userMessage);
                        return;
                    }

                    if (user.getSharedKey() != null) {
                        userMessage.setBody(decryptMessage(user, Base64.getDecoder().decode(userMessage.getBody())));
                    }
                    messageQueueBuffer.put(userMessage);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            userLeftNotify(user);
        }
    }

    private void shareServerPublicKey(User user) {
        try {
            BufferedWriter userWriter = user.getUserWriter();
            userWriter.flush();
            String msg = "serverPublicKey " + new String(Base64.getEncoder().encode(serverPublicKey.getEncoded()));
            userWriter.write(msg + '\n');
            userWriter.flush();
        } catch (IOException e) {
            userLeftNotify(user);
        }
    }

    private void sendMessagesTo(User user, UserMessage userMessage) {
        try {
            String msgToSend = userMessage.getMessage();
            BufferedWriter userWriter = user.getUserWriter();
            userWriter.flush();
            String msg = userMessage.getMessage();
            if (user.getSharedKey() != null) {
                msgToSend = new String(Base64.getEncoder().encode(encryptMessage(user, msg)));
            }
            userWriter.write(msgToSend +'\n');
            userWriter.flush();
        } catch (IOException e) {
            userLeftNotify(user);
        }
    }

    private void userLeftNotify(User user) {
        userQueue.remove(user);
        enqueueUserLeftMessage(user);
        printUserLeftMessage(user);
    }

    private void enqueueUserLeftMessage(User user) {
        try {
            UserMessage leftUserMessage = new UserMessage();
            leftUserMessage.setMessage(String.format(MESSAGE_USER_LEFT, user.getTrip()));
            messageQueueBuffer.put(leftUserMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printUserLeftMessage(User user) {
        GuiMessage consoleLeftGuiMessage = new GuiMessage(String.format(MESSAGE_USER_LEFT, user.getTrip()), Color.red, true);
        linkBucketGuiMessages.addMessage(consoleLeftGuiMessage);
    }

    public void checkUsersConnection() {
        char nullByte = 0b00000000;
        userQueue.forEach(user -> {
            try {
                BufferedWriter userWriter = user.getUserWriter();
                userWriter.write(nullByte);
                userWriter.flush();
            } catch (IOException e) {
                userLeftNotify(user);
            }
        });
    }


    public byte[] encryptMessage(User user, final String message) {
        byte[] encryptedMessage = new byte[0];
        try {
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(user.getSharedKey(), "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            encryptedMessage = cipher.doFinal(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMessage;
    }

    public String decryptMessage(User user, final byte[] message) {
        String decryptedMessage = null;
        try {
            // You can use Blowfish or another symmetric algorithm but you must adjust the key size.
            final SecretKeySpec keySpec = new SecretKeySpec(user.getSharedKey(), "DES");
            final Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            decryptedMessage = new String(cipher.doFinal(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }

    public void generateSharedKey(User user) {
        try {
            final KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(serverPrivateKey);
            keyAgreement.doPhase(user.getUserPublicKey(), true);

            user.setSharedKey(shortenSecretKey(keyAgreement.generateSecret()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateKeys() {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(1024);

            final KeyPair keyPair = keyPairGenerator.generateKeyPair();

            serverPrivateKey = keyPair.getPrivate();
            serverPublicKey = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserPublicKey(User user, byte[] userPublicKeyBytes) {
        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance("DH").generatePublic(new X509EncodedKeySpec(userPublicKeyBytes));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        user.setUserPublicKey(publicKey);
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

    public void start() {
        if (!isRunning) {
            executorScheduler.scheduleWithFixedDelay(this::getUserMessages, 0, 1, TimeUnit.MILLISECONDS);
            executorScheduler.scheduleWithFixedDelay(this::shareUserMessages, 0, 1, TimeUnit.MILLISECONDS);
            executorScheduler.scheduleWithFixedDelay(this::checkUsersConnection, 0, 3, TimeUnit.MILLISECONDS);
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            executorScheduler.shutdown();
            isRunning = false;
        }
    }
}
