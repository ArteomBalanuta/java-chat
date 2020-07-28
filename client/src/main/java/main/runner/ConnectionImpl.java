package main.runner;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class ConnectionImpl {
    //TODO move out here
    private final Charset enc = ISO_8859_1;

    private static volatile Socket connection;

    private BufferedReader userReader;
    private BufferedWriter userWriter;

    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    private InputStream is;
    private OutputStream os;

    public ConnectionImpl(String host, int port) {
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

    public void write(String msg) {
        if (msg != null) {
            try {
                userWriter.write(msg ); // + ((byte) 0x00)
                userWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
