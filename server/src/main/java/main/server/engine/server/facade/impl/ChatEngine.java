package main.server.engine.server.facade.impl;

import main.server.engine.console.models.GUIMessage;
import main.server.engine.console.service.GUIMessageService;
import main.server.engine.server.facade.Chat;
import main.server.engine.server.service.KeyService;
import main.server.engine.server.service.RSAService;
import main.server.engine.server.service.impl.KeyServiceImpl;
import main.server.engine.server.service.impl.RSAServiceImpl;
import main.server.models.message.UserMessage;
import main.server.models.user.User;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static main.server.utils.Constants.MESSAGE_USER_LEFT;

//TODO FIX
public class ChatEngine implements Chat {

    private static final int USERS_MAX_NUMBER = 50;
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final int THREAD_NUMBER = 2;

    private static Runnable readShareRunnable;

    private static ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);
    private static final BlockingQueue<UserMessage> messageQueueBuffer = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, false);
    private static final BlockingQueue<User> userQueue = new ArrayBlockingQueue<>(USERS_MAX_NUMBER, true);

    private final GUIMessageService guiMessageService;
    private final KeyService keyService = new KeyServiceImpl();
    private final RSAService rsaService = new RSAServiceImpl();

    private volatile boolean isRunning = true;

    private ScheduledFuture<?> scheduledFutureGetUserMessages = null;
    private ScheduledFuture<?> scheduledFutureShareUserMessages = null;
    private ScheduledFuture<?> scheduledFutureCheckUserConnections = null;

    public ChatEngine(GUIMessageService guiMessageService) {
        this.guiMessageService = guiMessageService;
        this.rsaService.setKeyService(keyService);
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

    private void getUserMessage(User user) {
        Optional<UserMessage> userMessageOptional = user.getMessage();
        if (userMessageOptional.isEmpty()) {
            return;
        }

        UserMessage userMessage = userMessageOptional.get();
        boolean isPublicKeyExchangeMessage = userMessage.isKeyExchangeMessage();

        if (isPublicKeyExchangeMessage) {
            proceedKeyMessage(user, userMessage);
            return;
        }

        boolean isSharedKeyPresent = user.getSharedKey() != null;
        if (isSharedKeyPresent) {
            userMessage.setBody(
                    rsaService.decryptMessage(user, Base64.getDecoder().decode(userMessage.getBody()))
            );
        }

        addMessageToMessageQueue(userMessage);
    }

    private void proceedKeyMessage(User user, UserMessage userMessage) {
        try {
            boolean isValidKey = validatePublicKey(user, userMessage);
            if (isValidKey) {
                userMessage = new UserMessage(user.getTrip(), "exchanged public key!");
                addMessageToMessageQueue(userMessage);
            } else {
                userMessage = new UserMessage(user.getTrip(), "key exchange failed!");
            }
            messageQueueBuffer.put(userMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
            userLeftNotify(user);
        }
    }

    private boolean validatePublicKey(User user, UserMessage userMessage) {
        int keyOffset = userMessage.getBody().indexOf("publicKey ") + 10;
        byte[] key = userMessage.getBody().substring(keyOffset).getBytes();
        if (keyService.validatePublicKey(key)) {
            keyService.setUserPublicKey(user, Base64.getDecoder().decode(key));
            keyService.shareServerPublicKeyToUser(user);
            keyService.generateAndSetSharedKeyToUser(user);
            return true;
        }
        return false;
    }

    void addMessageToMessageQueue(UserMessage userMessage) {
        try {
            messageQueueBuffer.put(userMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessagesTo(User user, UserMessage userMessage) {
        try {
            String msgToSend = userMessage.getMessage();
            BufferedWriter userWriter = user.getUserWriter();
            userWriter.flush();
            String msg = userMessage.getMessage();
            if (user.getSharedKey() != null) {
                msgToSend = new String(Base64.getEncoder().encode(rsaService.encryptMessage(user, msg)));
            }
            userWriter.write(msgToSend + '\n');
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
        GUIMessage consoleLeftGUIMessage = new GUIMessage(String.format(MESSAGE_USER_LEFT, user.getTrip()), Color.red, true);
        guiMessageService.addMessage(consoleLeftGUIMessage);
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

    private void cancelReceiveAndShareMessageSchedulers() {
        boolean interrupted = false;
        try {
            while (scheduledFutureGetUserMessages == null ||
                    scheduledFutureShareUserMessages == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            scheduledFutureGetUserMessages.cancel(false);
            scheduledFutureShareUserMessages.cancel(false);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //TODO: make sure it is not broken
    public void run() {
        new Thread(() -> {
            readShareRunnable = () -> {
                scheduledFutureGetUserMessages = executorScheduler.scheduleWithFixedDelay(this::getUserMessages, 0, 10, TimeUnit.MILLISECONDS);
                scheduledFutureShareUserMessages = executorScheduler.scheduleWithFixedDelay(this::shareUserMessages, 0, 10, TimeUnit.MILLISECONDS);
            };
            readShareRunnable.run();

            scheduledFutureCheckUserConnections = executorScheduler.scheduleWithFixedDelay(this::checkUsersConnection, 0, 10, TimeUnit.MILLISECONDS);
            while (true) {
                try {
                    Thread.sleep(100);
                    boolean isNotRunningAndSchedulersNotCancelled = !isRunning &&
                            !scheduledFutureGetUserMessages.isCancelled() &&
                            !scheduledFutureShareUserMessages.isCancelled();
                    if (isNotRunningAndSchedulersNotCancelled) {
                        cancelReceiveAndShareMessageSchedulers();
                    }

                    boolean isRunningAndSchedulersCancelled = isRunning &&
                            scheduledFutureGetUserMessages.isCancelled() &&
                            scheduledFutureShareUserMessages.isCancelled();
                    if (isRunningAndSchedulersCancelled) {
                        readShareRunnable.run();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
        }
    }
}
