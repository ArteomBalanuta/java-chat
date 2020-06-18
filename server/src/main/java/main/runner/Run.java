package main.runner;

import main.engine.BucketGuiMessage;
import main.engine.console.ConsoleEngine;
import main.engine.console.models.GuiMessage;
import main.engine.server.ChatEngine;
import main.models.user.User;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static main.utils.Constants.*;

public class Run {
    private static final int SERVER_PORT = 800;
    private static final int THREAD_NUMBER = 2;

    private final ServerSocket server;
    private final BucketGuiMessage bucketGuiMessages;
    private final ConsoleEngine consoleEngine;

    private final Thread listenNewConnectionsThread;
    private final Thread engineThread;
    private final Thread consoleThread;

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

    private Run(ServerSocket server, ChatEngine chatEngine, BucketGuiMessage bucketGuiMessages, ConsoleEngine consoleEngine) {
        this.server = server;
        this.bucketGuiMessages = bucketGuiMessages;;
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
        bucketGuiMessages.addMessage(consoleLeftGuiMessage);
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
        bucketGuiMessages.addMessage(serverUpGuiMessage);
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            BucketGuiMessage bucketGuiMessage = new BucketGuiMessage();
            ChatEngine chatEngine = new ChatEngine(bucketGuiMessage);
            ConsoleEngine consoleEngine = new ConsoleEngine();

            Run applicationServer = new Run(serverSocket, chatEngine, bucketGuiMessage, consoleEngine);
            applicationServer.startServer();

        } catch (Exception exception) {
            exception.printStackTrace();
            appExecutor.shutdown();
        }
    }
}
