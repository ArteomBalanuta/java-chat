package main.engine.console.service.impl;

import main.engine.console.models.command.Command;
import main.engine.console.models.command.Help;
import main.engine.console.models.command.Start;
import main.engine.console.models.command.Stop;
import main.engine.console.service.CMDService;
import main.engine.console.service.GUIService;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class CMDServiceImpl implements CMDService {
    private static final BlockingQueue<Command> commandQueue = new ArrayBlockingQueue<>(5);
    private static final List<Command> supportedCommands = new ArrayList<>();

    static {
        supportedCommands.add(new Help());
        supportedCommands.add(new Stop());
        supportedCommands.add(new Start());
    }

    private GUIService guiService;

    public CMDServiceImpl(GUIService guiService){
        this.guiService = guiService;
    }

    public void setUpKeyListener(){
        setKeyListener();
    }

    private Optional<Command> getCmdFromQueue() {
        Optional<Command> cmd = Optional.empty();
        try {
            cmd = Optional.of(commandQueue.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cmd;
    }

    private boolean isSupportedCommand(String cmd) {
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
        JTextField input = guiService.getInput();
        KeyListener keyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    enqueueCommand(input.getText().trim());
                }
            }
        };
        input.addKeyListener(keyListener);
    }

    public void execute() {
        getCmdFromQueue().ifPresent(Command::execute);
    }
}
