package main.engine.console;

import main.engine.BucketGuiMessage;
import main.engine.console.gui.Gui;
import main.engine.console.gui.GuiImpl;
import main.engine.console.models.Message;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ConsoleEngine {

    private static Gui gui;

    private static final int THREAD_NUMBER = 2;
    private final ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);

    private static final BlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(5);

    private void getAndPrintGuiMessages() {
        BlockingQueue<Message> messagesForGUI = BucketGuiMessage.getMessagesForGUI();
        boolean isEmptyBucket = messagesForGUI.isEmpty();
        if (isEmptyBucket) {
            try {
                Message guiMessage = messagesForGUI.take();
                print(guiMessage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void print(Message message) {
        gui.print(message);
    }

    private void clearConsole() {
        gui.clearInput();
        gui.clearOut();
    }

    private void clearOut() {
        gui.clearOut();
    }

    private void clearIn() {
        gui.clearInput();
    }

    private void setKeyListener() {
        JTextField input = gui.getInput();
        KeyListener keyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        String command = input.getText().trim();
                        commandQueue.put(command);
                    }
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        };
        input.addKeyListener(keyListener);
    }

    public ConsoleEngine() {
        gui = new GuiImpl();
        setKeyListener();
    }

    public void start() {
        executorScheduler.scheduleWithFixedDelay(this::getAndPrintGuiMessages, 0, 2, TimeUnit.MILLISECONDS);
    }
}
