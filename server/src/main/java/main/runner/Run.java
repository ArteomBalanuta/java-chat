package main.runner;

import main.engine.console.ConsoleEngine;
import main.engine.server.ChatEngine;
import main.models.user.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.*;
import static main.engine.console.ConsoleEngine.*;
import static main.utils.Constants.*;

public class Run {
    private static final int SERVER_PORT = 800;
    private static final int THREAD_NUMBER = 2;

    private final ServerSocket server;

    private final Thread listenNewConnectionsThread;
    private final Thread engineThread;

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

    private Run(ServerSocket server, ChatEngine chatEngine) {
        this.server = server;

        this.listenNewConnectionsThread = new Thread(this::listenForConnections);
        this.engineThread = new Thread(chatEngine::start);
    }

    private void saveUser(Socket socket) {
        User user = new User(socket);
        ChatEngine.saveUser(user);
        log(format(MESSAGE_USER_JOIN, user.getTrip()));
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
        log(format(SERVER_ONLINE, server.getInetAddress(), SERVER_PORT));
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            ChatEngine chatEngine = new ChatEngine();

            Run applicationServer = new Run(serverSocket, chatEngine);

            applicationServer.startServer();

        } catch (Exception exception) {
            exception.printStackTrace();
            appExecutor.shutdown();
        }
    }
}
