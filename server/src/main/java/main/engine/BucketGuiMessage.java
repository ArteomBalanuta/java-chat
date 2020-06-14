package main.engine;

import main.engine.console.models.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BucketGuiMessage {
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final BlockingQueue<Message> consoleMessageQueue = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, true);


    public void addMessage(Message message) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[ yyyy-MM-dd hh:mm:ss ]");
            String localDateTime = LocalDateTime.now().format(formatter);
            message.setMessage(localDateTime + " - " + message.getMessage() + '\n');
            consoleMessageQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BlockingQueue<Message> getMessagesForGUI() {
        return consoleMessageQueue;
    }

    ;
}
