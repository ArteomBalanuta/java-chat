package main.engine.console.facade.impl;

import main.engine.console.facade.GUIFacade;
import main.engine.console.service.GUIMessageService;
import main.engine.console.service.GUIService;
import main.engine.console.service.impl.CMDServiceImpl;
import main.engine.console.service.impl.GUIMessageServiceImpl;
import main.engine.console.service.impl.GUIServiceImpl;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class GUIFacadeImpl implements GUIFacade {
    private static final int THREAD_NUMBER = 2;
    private final ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);

    private GUIService guiService = new GUIServiceImpl();
    private GUIMessageService guiMessageService = new GUIMessageServiceImpl();
    private CMDServiceImpl cmdService = new CMDServiceImpl(guiService);

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

    public void startConsole() {
        executorScheduler.scheduleWithFixedDelay(this::printMessages, 0, 2, TimeUnit.MILLISECONDS);
        executorScheduler.scheduleWithFixedDelay(this::executeCMDs, 0, 2, TimeUnit.MILLISECONDS);
    }


}
