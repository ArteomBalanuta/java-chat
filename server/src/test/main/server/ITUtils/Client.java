package main.server.ITUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class Client {
    //ISO_8859_1
    private Charset enc;
    private Socket connection;

    private BufferedReader userReader;
    private BufferedWriter userWriter;

    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    private InputStream is;
    private OutputStream os;

    public void setEnc(Charset enc) {
        this.enc = enc;
    }

    public void connect(String host, int port) {
        try {
            this.connection = new Socket(host, port);

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
            char[] charBuffer = new char[100];
            char c;

            int i = 0;
            while (userReader.ready()) {
                c = (char) userReader.read();
                if (c != '\n') {
                    charBuffer[i] = c;
                    i++;
                } else {
                    msg = new String(charBuffer);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
