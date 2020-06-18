package main.engine.console;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.*;

import main.engine.console.gui.Gui;
import main.engine.console.models.GuiMessage;
import main.engine.console.models.command.Command;
import main.engine.console.models.command.Help;
import main.engine.console.models.command.Start;
import main.engine.console.models.command.Stop;
import main.models.dto.LinkBucketGuiMessage;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ConsoleEngine  {

    private static Gui gui;

    private static final int THREAD_NUMBER = 2;
    private final ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);

    private static final List<Command> supportedCommands = new ArrayList<>();
    private static final BlockingQueue<Command> commandQueue = new ArrayBlockingQueue<>(5);

    private Optional<Command> takeCmdFromCommandQueue() {
        Optional<Command> cmd = Optional.empty();
        try {
            cmd = Optional.of(commandQueue.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cmd;
    }

    private void printBucketMessages() {
        getMessageFromBucket().ifPresent(this::printMessage);
    }

    private void processConsoleCmd() {
        takeCmdFromCommandQueue().ifPresent(Command::execute);
    }

    private Optional<GuiMessage> getMessageFromBucket() {
        boolean isEmptyBucket = LinkBucketGuiMessage.getMessagesForGUI().isEmpty();
        if (isEmptyBucket) {
            try {
                return Optional.of(LinkBucketGuiMessage.getMessagesForGUI().take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    private void printMessage(GuiMessage guiMessage) {
        gui.print(guiMessage);
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

    private boolean isSupportedCommand(String cmd){
        return supportedCommands.stream().anyMatch(c -> c.getCommandString().equals(cmd));
    }

    private void enqueueCommand(String cmd) {
        try {
            if (isSupportedCommand(cmd)) {
                commandQueue.put(supportedCommands.stream()
                    .filter(c -> c.getCommandString().equals(cmd))
                    .collect(Collectors.toList()).get(0));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setKeyListener() {
        JTextField input = gui.getInput();
        KeyListener keyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    enqueueCommand(input.getText().trim());
                }
            }
        };
        input.addKeyListener(keyListener);
    }

    public ConsoleEngine(Gui newGui) {
        ConsoleEngine.gui = newGui;
        setKeyListener();
        supportedCommands.add(new Help());
        supportedCommands.add(new Stop());
        supportedCommands.add(new Start());
    }

    public void startConsoleEngine() {
        executorScheduler.scheduleWithFixedDelay(this::printBucketMessages, 0, 2, TimeUnit.MILLISECONDS);
        executorScheduler.scheduleWithFixedDelay(this::processConsoleCmd, 0, 2, TimeUnit.MILLISECONDS);
    }
}
