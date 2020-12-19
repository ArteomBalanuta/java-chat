package main.server;

import main.server.engine.console.facade.GUIFacade;
import main.server.engine.console.facade.impl.GUIFacadeImpl;
import main.server.engine.console.models.GUIMessage;
import main.server.engine.console.models.command.Command;
import main.server.engine.console.service.CMDService;
import main.server.engine.console.service.GUIMessageService;
import main.server.engine.console.service.GUIService;
import main.server.engine.console.service.impl.CMDServiceImpl;
import main.server.engine.console.service.impl.GUIMessageServiceImpl;
import main.server.engine.console.service.impl.GUIServiceImpl;
import main.server.engine.server.facade.ChatEngine;
import main.server.engine.server.facade.impl.ChatEngineImpl;
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
public class ApplicationRunner {
    private static final int SERVER_PORT = 800;
    private static final int THREAD_NUMBER = 2;

    private ServerSocket serverSocket;
    private GUIFacade guiFacade;
    private GUIMessageService guiMessageService;
    private ChatEngine chatEngine;

    private Thread listenNewConnectionsThread;
    private Thread engineThread;
    private Thread consoleThread;

    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

    public ApplicationRunner(GUIFacade guiFacade, GUIMessageService guiMessageService, ChatEngine chatEngine) throws IOException {
        this.guiFacade = guiFacade;
        this.guiMessageService = guiMessageService;
        this.chatEngine = chatEngine;

        this.listenNewConnectionsThread = new Thread(this::listenForConnections);
        this.engineThread = new Thread(chatEngine::run);
        this.consoleThread = new Thread(guiFacade::startConsole);

        this.serverSocket = new ServerSocket(SERVER_PORT);
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

    private void saveUser(Socket socket) {
        User user = new User(socket);
        ChatEngineImpl.saveUser(user);

        GUIMessage consoleJoinGUIMessage =
                new GUIMessage(format(MESSAGE_USER_JOIN, user.getTrip()), Color.red, true);
        guiMessageService.addMessage(consoleJoinGUIMessage);
    }

    private void startServer() {
        appExecutor.submit(listenNewConnectionsThread);
        appExecutor.submit(engineThread);
        appExecutor.submit(consoleThread);

        GUIMessage serverUpGUIMessage = new GUIMessage(
                format(SERVER_ONLINE, serverSocket.getInetAddress(), SERVER_PORT),
                Color.GRAY,
                true );

        guiMessageService.addMessage(serverUpGUIMessage);
    }

    public static void main(String[] args) {
        try {
            GUIService guiService = new GUIServiceImpl();
            GUIMessageService guiMessageService = new GUIMessageServiceImpl();
            ChatEngine chatEngine = new ChatEngineImpl(guiMessageService);
            CMDService cmdService = new CMDServiceImpl(guiService);

            GUIFacade guiFacade = new GUIFacadeImpl(guiService, guiMessageService, cmdService);
            ApplicationRunner applicationRunner = new ApplicationRunner(guiFacade, guiMessageService , chatEngine);

            Command.setUp(guiService, chatEngine);
            applicationRunner.startServer();

        } catch (Exception exception) {
            exception.printStackTrace();
            appExecutor.shutdown();
        }
    }
}
