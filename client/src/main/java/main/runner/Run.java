package main.runner;

import java.io.*;
import java.net.Socket;

public class Run {

    private static volatile Socket connection;
    private static DataOutputStream outputStream;

    private static PrintStream out;

    void acquireConnection() {
        try {
            connection = new Socket("localhost", 800);
            outputStream = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processTheMessage() {
        String message = getMsg();
        sendMessage(message);
    }

    String getMsg() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                Thread.sleep(20);
                return reader.readLine();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void sendMessage(String msg) {
        if (msg != null) {
            try {
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
        //reading msg and send thread
        new Thread(application::processTheMessage).start();

        //receive and print thread
//            new Thread(ap).start();

    }
}
