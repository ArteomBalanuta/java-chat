package main.runner;

import main.engine.Engine;
import main.models.user.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static main.utils.Constants.*;
import static main.utils.Utils.log;

public class Run {
    private static final int SERVER_PORT = 800;
    private static final int THREAD_NUMBER = 2;

    private final ServerSocket server;

    private final Thread listenThread;
    private final Thread engineThread;

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

    private Run(ServerSocket server, Engine engine) {
        this.server = server;
        this.listenThread = new Thread(this::listenForConnections);
        this.engineThread = new Thread(engine::start);
    }

    private static void saveUser(Socket socket) {
        User user = new User(socket);
        Engine.saveUser(user);
        log(String.format(MESSAGE_USER_JOIN, user.getTrip()));
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
        appExecutor.submit(listenThread);
        appExecutor.submit(engineThread);
        log(SERVER_STARTED);
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Engine engine = new Engine();
            Run applicationServer = new Run(serverSocket, engine);

            applicationServer.startServer();
            log(String.format(SERVER_ONLINE, serverSocket.getInetAddress(), SERVER_PORT));

        } catch (Exception exception) {
            exception.printStackTrace();
            appExecutor.shutdown();
        }
    }
}
