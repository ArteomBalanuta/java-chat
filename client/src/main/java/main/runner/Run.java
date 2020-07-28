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
    private final Charset enc = ISO_8859_1;

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    public static final String HOST_ADDRESS = "localhost";
    public static final int PORT = 800;

    private static volatile Socket connection;

    private BufferedReader userReader;
    private BufferedWriter userWriter;

    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    private InputStream is;
    private OutputStream os;

    private final List<String> msgOutQueue = new ArrayList<>();
    private final List<String> msgInQueue = new ArrayList<>();

    public static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService appExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static final ScheduledExecutorService executorScheduler = newScheduledThreadPool(NUMBER_OF_THREADS);;

    private void connect(String host, int port) {
        try {
            connection = new Socket(host, port);

            this.is = connection.getInputStream();
            this.os = connection.getOutputStream();

            this.inputStreamReader = new InputStreamReader(is, enc);
            this.outputStreamWriter = new OutputStreamWriter(os, enc);

            this.userReader = new BufferedReader(inputStreamReader);
            this.userWriter = new BufferedWriter(outputStreamWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        if (msg != null) {
            try {
                userWriter.write(msg + "\n");
                userWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveMessages() {
        try {
            if (userReader.ready()) {
                String msgBody = userReader.readLine();
                if (msgBody != null && !msgBody.isEmpty()) {
                    msgInQueue.add(msgBody.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printInMessages(){
        Optional<String> msg = getFromQueue(msgInQueue);
        msg.ifPresent(System.out::println);
    }

    public static void main(String[] args) {
        Run application = new Run();
        application.connect(HOST_ADDRESS, PORT);

        appExecutor.submit(new Thread(application::readUserInput));
        appExecutor.submit(new Thread(application::sendUserMessages));
        appExecutor.submit(new Thread(application::printIncomeMessages));
    }
}
