package main.models.dto;

import main.engine.console.models.GUIMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//TODO FIX
public class LinkBucketGuiMessage {
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final BlockingQueue<GUIMessage> CONSOLE_GUI_MESSAGE_QUEUE = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, true);

    public void addMessage(GUIMessage guiMessage) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[ yyyy-MM-dd hh:mm:ss ]");
            String localDateTime = LocalDateTime.now().format(formatter);
            guiMessage.setMessage(localDateTime + " - " + guiMessage.getMessage() + '\n');
            CONSOLE_GUI_MESSAGE_QUEUE.put(guiMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BlockingQueue<GUIMessage> getMessagesForGUI() {
        return CONSOLE_GUI_MESSAGE_QUEUE;
    }
}
