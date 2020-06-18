package main.runner;

import main.engine.console.gui.Gui;
import main.engine.console.gui.GuiImpl;
import main.engine.server.Chat;
import main.engine.server.ChatEngine;
import main.models.dto.LinkBucketGuiMessage;
import main.engine.console.ConsoleEngine;
import main.engine.console.models.GuiMessage;
import main.models.dto.LinkChatEngine;
import main.models.dto.LinkGui;
import main.models.user.User;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static main.utils.Constants.*;

//TODO REFACTOR
public class Run {
    private static final int SERVER_PORT = 800;
    private static final int THREAD_NUMBER = 2;

    static LinkBucketGuiMessage bucket = new LinkBucketGuiMessage();
    static Gui gui = new GuiImpl();
    static Chat chat = new ChatEngine(bucket);
    static {
        LinkGui.setGui(gui);
        LinkChatEngine.setChat(chat);
    }

    private final ServerSocket server;
    private final LinkBucketGuiMessage linkBucketGuiMessages;
    private final ConsoleEngine consoleEngine;

    private final Thread listenNewConnectionsThread;
    private final Thread engineThread;
    private final Thread consoleThread;

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

    private Run(ServerSocket server, ChatEngine chatEngine, LinkBucketGuiMessage linkBucketGuiMessages, ConsoleEngine consoleEngine) {
        this.server = server;
        this.linkBucketGuiMessages = linkBucketGuiMessages;;
        this.consoleEngine = consoleEngine;

        this.listenNewConnectionsThread = new Thread(this::listenForConnections);
        this.engineThread = new Thread(chatEngine::start);
        this.consoleThread = new Thread(consoleEngine::startConsoleEngine);
    }

    private void saveUser(Socket socket) {
        User user = new User(socket);
        ChatEngine.saveUser(user);

        GuiMessage consoleLeftGuiMessage =
                new GuiMessage(format(MESSAGE_USER_JOIN, user.getTrip()), Color.red, true);
        linkBucketGuiMessages.addMessage(consoleLeftGuiMessage);
    }

    private void listenForConnections() {
        while (true) {
            try {
                Socket socket = server.accept();
                saveUser(socket);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void startServer() {
        appExecutor.submit(listenNewConnectionsThread);
        appExecutor.submit(engineThread);
        appExecutor.submit(consoleThread);

        GuiMessage serverUpGuiMessage =
                new GuiMessage(format(SERVER_ONLINE, server.getInetAddress(), SERVER_PORT), Color.GRAY, true);
        linkBucketGuiMessages.addMessage(serverUpGuiMessage);
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            ConsoleEngine consoleEngine = new ConsoleEngine(gui);
            Run applicationServer = new Run(serverSocket, (ChatEngine) chat, bucket, consoleEngine);
            applicationServer.startServer();

        } catch (Exception exception) {
            exception.printStackTrace();
            appExecutor.shutdown();
        }
    }
}
