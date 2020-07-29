package main.runner;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class ConnectionImpl {
    //TODO move out here
    private final Charset enc = ISO_8859_1;

    private static volatile Socket connection;

    private static BufferedReader userReader;
    private static BufferedWriter userWriter;

    private static InputStreamReader inputStreamReader;
    private static OutputStreamWriter outputStreamWriter;

    private static InputStream is;
    private static OutputStream os;

    public ConnectionImpl(String host, int port) {
        try {
            connection = new Socket(host, port);

            is = connection.getInputStream();
            os = connection.getOutputStream();

            inputStreamReader = new InputStreamReader(is, enc);
            outputStreamWriter = new OutputStreamWriter(os, enc);

            userReader = new BufferedReader(inputStreamReader);
            userWriter = new BufferedWriter(outputStreamWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) {
            try {
                userWriter.flush();
                userWriter.write(msg + '\n');
                userWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public String read() {
        String msg = null;
        try {
            if (userReader.ready()) {
                msg = userReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
