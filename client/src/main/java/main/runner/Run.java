package main.runner;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Run {
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private static volatile Socket connection;
    private static DataOutputStream outputStream;

    private static PrintStream out;

    private final List<String> msgQueue = new ArrayList<>();

    void acquireConnection() {
        try {
            connection = new Socket("localhost", 800);
            outputStream = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized String getFromQueue() {
        String msg = null;
        if (msgQueue.size() != 0) {
            msg = msgQueue.get(msgQueue.size() - 1);
            msgQueue.remove(msgQueue.size() - 1);
        }
        return msg;
    }

    void addMsg() {
        while (true) {
            try {
                Thread.sleep(50);

                String msg = reader.readLine();
                msgQueue.add(msg);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    void getSendLoop() {
        while (true) {
            try {
                Thread.sleep(20);
                String toSend = getFromQueue();
                sendMessage(toSend);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void sendMessage(String msg) {
        if (msg != null) {
            try {
                System.out.println("S: " + msg);
                outputStream.writeUTF(msg);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Run application = new Run();
        application.acquireConnection();

        //read and enqueue msg
        new Thread(application::addMsg).start();

        //send
        new Thread(application::getSendLoop).start();

    }
}
