package main.engine.server;

import main.models.dto.LinkBucketGuiMessage;
import main.engine.console.models.GuiMessage;
import main.models.message.UserMessage;
import main.models.user.User;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static main.utils.Constants.MESSAGE_USER_LEFT;
import static main.utils.Utils.isNotNullOrEmpty;


public class ChatEngine implements Chat  {

    private static final int USERS_MAX_NUMBER = 50;
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final int THREAD_NUMBER = 2;

    private static ScheduledExecutorService executorScheduler;
    private static final BlockingQueue<UserMessage> messageQueueBuffer = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, false);
    private static final BlockingQueue<User> userQueue = new ArrayBlockingQueue<>(USERS_MAX_NUMBER, true);

    public final LinkBucketGuiMessage linkBucketGuiMessages;
    private boolean isRunning = false;

    public ChatEngine(LinkBucketGuiMessage linkBucketGuiMessage){
        linkBucketGuiMessages = linkBucketGuiMessage;
    };

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
            UserMessage userMessage = messageQueueBuffer.take();
            for (User user : userQueue) {
                sendMessagesTo(user, userMessage);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getUserMessage(User user) {
        try {
            BufferedReader reader = user.getUserReader();
            if (reader.ready()) {
                String msgBody = reader.readLine();
                if (isNotNullOrEmpty(msgBody)) {
                    UserMessage userMessage = new UserMessage(user.getTrip(), msgBody);
                    messageQueueBuffer.put(userMessage);
                }
            }
        } catch (IOException | InterruptedException e) {
            userLeftNotify(user);
        }
    }

    private void sendMessagesTo(User user, UserMessage userMessage) {
        try {
            BufferedWriter userWriter = user.getUserWriter();
            userWriter.write(userMessage.getMessage());
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

    private void enqueueUserLeftMessage(User user){
        try {
            UserMessage leftUserMessage = new UserMessage();
            leftUserMessage.setMessage(String.format(MESSAGE_USER_LEFT, user.getTrip()));
            messageQueueBuffer.put(leftUserMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printUserLeftMessage(User user){
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

    public void start() {
        if (!isRunning) {
            executorScheduler = newScheduledThreadPool(THREAD_NUMBER);
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
