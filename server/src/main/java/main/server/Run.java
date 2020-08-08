package main.server;

import main.server.engine.console.facade.GUIFacade;
import main.server.engine.console.facade.impl.GUIFacadeImpl;
import main.server.engine.console.models.GUIMessage;
import main.server.engine.console.models.command.Command;
import main.server.engine.console.service.GUIMessageService;
import main.server.engine.server.facade.Chat;
import main.server.engine.server.facade.impl.ChatEngine;
import main.server.models.user.User;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static main.server.utils.Constants.MESSAGE_USER_JOIN;
import static main.server.utils.Constants.SERVER_ONLINE;

//TODO REFACTOR
public class Run {
    private static final int SERVER_PORT = 800;
    private static final int THREAD_NUMBER = 2;

    private ServerSocket serverSocket;
    private GUIFacade guiFacade = new GUIFacadeImpl();

    private GUIMessageService bucket = guiFacade.getGuiMessageService();
    private Chat chat = new ChatEngine(bucket);

    private Thread listenNewConnectionsThread = new Thread(this::listenForConnections);
    private Thread engineThread = new Thread(chat::run);
    private Thread consoleThread = new Thread(guiFacade::startConsole);

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

    void setUp() {
        try {
            this.serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUser(Socket socket) {
        User user = new User(socket);
        ChatEngine.saveUser(user);

        GUIMessage consoleJoinGUIMessage =
                new GUIMessage(format(MESSAGE_USER_JOIN, user.getTrip()), Color.red, true);
        bucket.addMessage(consoleJoinGUIMessage);
    }

    private void listenForConnections() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
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

        GUIMessage serverUpGUIMessage =
                new GUIMessage(format(SERVER_ONLINE, serverSocket.getInetAddress(), SERVER_PORT), Color.GRAY, true);
        bucket.addMessage(serverUpGUIMessage);
    }

    public static void main(String[] args) {
        try {
            Run applicationServer = new Run();
            //TODO FIX!
            Command.setUp(applicationServer.guiFacade.getGuiService(), applicationServer.chat);
            applicationServer.setUp();
            applicationServer.startServer();

        } catch (Exception exception) {
            exception.printStackTrace();
            appExecutor.shutdown();
        }
    }
}
