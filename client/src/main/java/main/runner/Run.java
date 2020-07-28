package main.runner;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class Run {
    public static final String HOST_ADDRESS = "localhost";
    public static final int PORT = 800;
    ConnectionImpl connection = new ConnectionImpl(HOST_ADDRESS, PORT);

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private final List<String> msgOutQueue = new ArrayList<>();
    private final List<String> msgInQueue = new ArrayList<>();

    public static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static final ScheduledExecutorService executorScheduler = newScheduledThreadPool(NUMBER_OF_THREADS);;

    synchronized Optional<String> getFromQueue(List<String> queue) {
        Optional<String> msg = Optional.empty();
        if (queue.size() != 0) {
            msg = Optional.ofNullable(queue.get(queue.size() - 1));
            queue.remove(queue.size() - 1);
        }
        return msg;
    }

    void readUserInput(){
        executorScheduler.scheduleWithFixedDelay(this::readFromInput, 0, 3, TimeUnit.MILLISECONDS);
    }

    void sendUserMessages(){
        executorScheduler.scheduleWithFixedDelay(this::sendMessages, 0, 3, TimeUnit.MILLISECONDS);
    }

    void printIncomeMessages(){
        executorScheduler.scheduleWithFixedDelay(this::receiveMessages, 0, 3, TimeUnit.MILLISECONDS);
        executorScheduler.scheduleWithFixedDelay(this::printInMessages, 0, 3, TimeUnit.MILLISECONDS);
    }

    void readFromInput() {
        try {
            String msg = reader.readLine();
            msgOutQueue.add(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMessages() {
        Optional<String> toSend = getFromQueue(msgOutQueue);
        sendMessage(toSend.orElse(null));
    }

    //TODO FIX
    void sendMessage(String msg) {
        connection.write(msg);
    }

    private void receiveMessages() {
        String msg = connection.read();
        if (msg != null && !msg.isEmpty()) {
            msgInQueue.add(msg.trim());
        }
    }

    private void printInMessages(){
        Optional<String> msg = getFromQueue(msgInQueue);
        msg.ifPresent(System.out::println);
    }

    public static void main(String[] args) {
        Run application = new Run();

        appExecutor.submit(new Thread(application::readUserInput));
        appExecutor.submit(new Thread(application::sendUserMessages));
        appExecutor.submit(new Thread(application::printIncomeMessages));
    }
}
