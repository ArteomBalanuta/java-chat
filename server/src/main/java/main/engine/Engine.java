package main.engine;

import main.engine.console.ConsoleEngine;
import main.engine.console.models.Message;
import main.engine.server.ChatEngine;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

//TODO Use this class for inits instead of Run.
public class Engine {
//    private static final int THREAD_NUMBER = 2;
//    private final ScheduledExecutorService executorScheduler = newScheduledThreadPool(THREAD_NUMBER);
//
//    BucketGuiMessage bucketGuiMessages = new BucketGuiMessage();
//    ChatEngine chatEngine = new ChatEngine(bucketGuiMessages);
//
//    boolean isRunning = false;
//
//     public void start() {
//        executorScheduler.scheduleWithFixedDelay(consoleEngine::parseCommands, 0, 100, TimeUnit.MILLISECONDS);
//        chatEngine.start();
//    }
//
//    public void stop(){
//         chatEngine.stop();
//    }


}
