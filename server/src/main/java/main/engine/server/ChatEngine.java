package main.engine.server;

import main.models.message.Message;
import main.models.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static main.engine.console.ConsoleEngine.log;
import static main.utils.Constants.MESSAGE_USER_LEFT;
import static main.utils.Utils.isNotNullOrEmpty;


public class ChatEngine {
    private static final int USERS_MAX_NUMBER = 50;
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final int THREAD_NUMBER = 2;

    private static final BlockingQueue<Message> messageQueueBuffer = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, true);
    private static final BlockingQueue<User> userQueue = new ArrayBlockingQueue<>(USERS_MAX_NUMBER, true);

    private final ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);

    public static void saveUser(User user) {
        try {
            userQueue.put(user);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getMessages() {
        userQueue.forEach(this::getMessageFrom);
    }

    private void sendMessages() {
        try {
            for (Object ignored : messageQueueBuffer) {
                Message message = messageQueueBuffer.take();
                for (User user : userQueue) {
                    sendMessagesTo(user, message);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getMessageFrom(User user) {
        try {
            BufferedReader reader = user.getUserReader();
            boolean isReady = reader.ready();
            if (isReady) {
                String msgBody = reader.readLine();
                if (isNotNullOrEmpty(msgBody)) {
                    Message message = new Message(user.getTrip(), msgBody);
                    messageQueueBuffer.put(message);
                }
            }
        } catch (IOException | InterruptedException e) {
            try {
            Message leftMessage = new Message();
            leftMessage.setMessage(String.format(MESSAGE_USER_LEFT, user.getTrip()));
            messageQueueBuffer.put(leftMessage);
            userQueue.remove(user);
            log(String.format(MESSAGE_USER_LEFT, user.getTrip()));
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    private void sendMessagesTo(User user, Message message) {
        try {
            BufferedWriter userWriter = user.getUserWriter();
            userWriter.write(message.getMessage());
            userWriter.flush();
        } catch (IOException e) {
            userLeftNotify(user);
        }
    }

    private void userLeftNotify(User user) {
        try {
            Message leftMessage = new Message();
            leftMessage.setMessage(String.format(MESSAGE_USER_LEFT, user.getTrip()));
            messageQueueBuffer.put(leftMessage);
            userQueue.remove(user);
            log(String.format(MESSAGE_USER_LEFT, user.getTrip()));
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    private void checkUserConnection() {
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

    public void start() {
        executorScheduler.scheduleWithFixedDelay(this::getMessages, 0, 1, TimeUnit.MILLISECONDS);
        executorScheduler.scheduleWithFixedDelay(this::sendMessages, 0, 1, TimeUnit.MILLISECONDS);
        executorScheduler.scheduleWithFixedDelay(this::checkUserConnection, 0, 3, TimeUnit.MILLISECONDS);
    }
}
