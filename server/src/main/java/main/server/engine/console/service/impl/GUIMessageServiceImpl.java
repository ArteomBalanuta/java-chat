package main.server.engine.console.service.impl;

import main.server.engine.console.models.GUIMessage;
import main.server.engine.console.service.GUIMessageService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GUIMessageServiceImpl implements GUIMessageService {
    private static final int MESSAGES_MAX_NUMBER = 300;
    private final BlockingQueue<GUIMessage> CONSOLE_GUI_MESSAGE_QUEUE = new ArrayBlockingQueue<>(MESSAGES_MAX_NUMBER, true);

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

    public Optional<GUIMessage> getMessage() {
        Optional<GUIMessage> guiMessage = Optional.empty();
        try {
            guiMessage = Optional.of(CONSOLE_GUI_MESSAGE_QUEUE.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return guiMessage;
    }
}
