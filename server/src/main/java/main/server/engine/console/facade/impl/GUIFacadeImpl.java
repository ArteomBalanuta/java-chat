package main.server.engine.console.facade.impl;

import main.server.engine.console.facade.GUIFacade;
import main.server.engine.console.models.GUIMessage;
import main.server.engine.console.service.CMDService;
import main.server.engine.console.service.GUIMessageService;
import main.server.engine.console.service.GUIService;
import main.server.engine.console.service.impl.CMDServiceImpl;
import main.server.engine.console.service.impl.GUIMessageServiceImpl;
import main.server.engine.console.service.impl.GUIServiceImpl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class GUIFacadeImpl implements GUIFacade {
    private GUIService guiService;
    private GUIMessageService guiMessageService;
    private CMDService cmdService;

    public GUIFacadeImpl(GUIService guiService, GUIMessageService guiMessageService, CMDService cmdService) {
        this.guiService = guiService;
        this.guiMessageService = guiMessageService;
        this.cmdService = cmdService;
    }

    private static final int THREAD_NUMBER = 2;
    private final ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);

    private void clearConsole() {
        guiService.clearInput();
        guiService.clearOut();
    }

    private void clearOut() {
        guiService.clearOut();
    }

    private void clearIn() {
        guiService.clearInput();
    }

    private void printMessages() {
        guiMessageService.getMessage().ifPresent(guiService::print);
    }

    private void executeCMDs() {
        cmdService.execute();
    }

    //TODO: MOVE SOMEWHERE SET UP KEY LISTENER
    public void startConsole() {
        executorScheduler.scheduleWithFixedDelay(this::printMessages, 0, 2, TimeUnit.MILLISECONDS);
        executorScheduler.scheduleWithFixedDelay(this::executeCMDs, 0, 2, TimeUnit.MILLISECONDS);
    }

    public GUIService getGuiService() {
        return guiService;
    }

    public GUIMessageService getGuiMessageService() {
        return guiMessageService;
    }

}
