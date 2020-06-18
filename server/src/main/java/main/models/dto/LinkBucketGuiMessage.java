package main.models.dto;

import main.engine.console.models.GuiMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LinkBucketGuiMessage {
    private static final int MESSAGES_MAX_NUMBER = 300;
    private static final BlockingQueue<GuiMessage> CONSOLE_GUI_MESSAGE_QUEUE = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, true);

    public void addMessage(GuiMessage guiMessage) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[ yyyy-MM-dd hh:mm:ss ]");
            String localDateTime = LocalDateTime.now().format(formatter);
            guiMessage.setMessage(localDateTime + " - " + guiMessage.getMessage() + '\n');
            CONSOLE_GUI_MESSAGE_QUEUE.put(guiMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BlockingQueue<GuiMessage> getMessagesForGUI() {
        return CONSOLE_GUI_MESSAGE_QUEUE;
    }
}
