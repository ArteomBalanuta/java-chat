package main.engine.console;

import main.engine.console.gui.Gui;
import main.engine.console.gui.GuiImpl;
import main.engine.console.models.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ConsoleEngine {
    private static final ScheduledExecutorService executorScheduler = newScheduledThreadPool(1);
    private static final BlockingQueue<String> commandQueue = new ArrayBlockingQueue<>(1);

    private static final Gui gui = new GuiImpl();

    public static void log(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[ yyyy-MM-dd hh:mm:ss ]");
        String localDateTime = LocalDateTime.now().format(formatter);
        String logString = localDateTime + " - " + str;

        Message logMessage = new Message(logString, Color.GREEN, true);
        print(logMessage);
    }

    private static void print(Message message) {
        gui.print(message);
    }

    private static void clearConsole() {
        gui.clearInput();
        gui.clearOut();
    }

    private static void clearOut() {
        gui.clearOut();
    }

    private static void clearIn() {
        gui.clearInput();
    }

    private static void parseCommands() {
        try {
            if (!commandQueue.isEmpty()) {
                String cmd = commandQueue.take();
                Message cmdMessage = new Message(cmd);
                switch (cmdMessage.getMessage()) {
                    case "help":
                        print(cmdMessage);
                        break;
                    case "":
                        cmdMessage.setMessage("Enter valid command!");
                        print(cmdMessage);
                        break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void setKeyListener() {
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
        setKeyListener();
        executorScheduler.scheduleWithFixedDelay(ConsoleEngine::parseCommands, 0, 100, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {

//        Message msg = new Message("TEST", Color.CYAN, true);
//        print(msg);

//                    //menu screen
//                    switch (newcommand) {
//                        case "help": {
//                            help();
//                            break;
//                        }
//                        case "clear": {
//                            clear();
//                            break;
//                        }
//                        case "reset": {
//                            map.clear();
//                            dataMap.clear();
//                            clear();
//                            break;
//                        }
//                        case "redcommand": {
//                            consolePrint(newcommand, Color.RED, true);
//                            break;
//                        }
//
//                        case "greencommand": {
//                            consolePrint(newcommand, Color.GREEN, true);
//                            break;
//                        }
//                        case "whitecommand": {
//                            consolePrint(newcommand, Color.WHITE, true);
//                            break;
//                        }
//
//                        //generateTableSchema(address,tablename);
//
//                        default: {
//                            consolePrint(newcommand, Color.LIGHT_GRAY, true);
//                        }
//                    }
//
//                    if (newcommand.contains("check")) {
//                        String[] arguments = newcommand.split("\\s+");
//                        if (!Objects.equals(arguments[1], "all")) {
//                            check(arguments[1]);
//                        }
//                        if (Objects.equals(arguments[1], "all")) {
//                            int numConns = map.size();
//                            consolePrint("Total Servers - " + numConns, Color.orange, true);
//                            map.forEach((k, v) -> check(k));
//                        }
//                    }
//
//                    if (newcommand.contains("monitor")) {
//                        String[] arguments = newcommand.split("\\s+");
//                        Thread thread = new Thread(() -> {
//                            while (flag) {
//                                try {
//                                    TimeUnit.SECONDS.sleep(3);
//                                    clear();
//                                    int numConns = map.size();
//                                    consolePrint("Total Servers - " + numConns, Color.orange, true);
//                                    map.forEach((k, v) -> check(k));
//                                    if (Thread.interrupted()) {
//                                        break;
//                                    }
//                                    TimeUnit.SECONDS.sleep(27);
//                                } catch (InterruptedException e1) {
//                                    Thread.currentThread().interrupt();
//                                }
//                            }
//                            Thread.currentThread().interrupt();
//                        });
//
//                        if (Objects.equals(arguments[1], "start")) {
//                            setFlag(true);
//                            mainFrame.setSize(1236, 500);
//                            thread.start();
//                            consolePrint("[Monitoring] ", Color.BLUE, false);
//                            consolePrint("Started", Color.WHITE, true);
//
//                        }
//                        if (Objects.equals(arguments[1], "stop")) {
//                            setFlag(false);
//                            mainFrame.setSize(700, 540);
//                            thread.interrupt();
//                            consolePrint("[Monitoring] ", Color.BLUE, false);
//                            consolePrint("Stopped", Color.WHITE, true);
//                        }
//                    }
//                    if (newcommand.contains("register")) {
//                        String[] arguments = newcommand.split("\\s+");
//                        Record record = new Record();
//                        if (Objects.equals(arguments[1], "win")) {
//                            record.setLogType(arguments[1]);
//                            record.setAddress(arguments[2]);
//                            record.setPort(arguments[3]);
//                            record.setDb(arguments[4]);
//                        }
//                        if (Objects.equals(arguments[1], "login")) {
//                            record.setLogType(arguments[1]);
//                            record.setAddress(arguments[2]);
//                            record.setPort(arguments[3]);
//                            record.setDb(arguments[4]);
//                            record.setUsername(arguments[5]);
//                            record.setPassword(arguments[6]);
//                        }
//
//                        if (Objects.equals(arguments[1], "informix")) {
//                            record.setLogType(arguments[1]);
//                            record.setAddress(arguments[2]);
//                            record.setPort(arguments[3]);
//                            record.setDb(arguments[4]);
//                            record.setUsername(arguments[5]);
//                            record.setPassword(arguments[6]);
//                        }
//                        dataMap.put(record.getAddress(), record);

//                        if (registerConnection(record)) {
//                            consolePrint("Registration succeeded!", Color.GREEN, true);
//                        } else {
//                            consolePrint("Registration failed!", Color.RED, true);
//                        }
//                }
//            }
//        });
    }
}
