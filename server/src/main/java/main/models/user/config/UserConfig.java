package main.models.user.config;


import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class UserConfig {
    private final Charset enc = ISO_8859_1;

    private Socket connection;

    private BufferedReader userReader;
    private BufferedWriter userWriter;

    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    private InputStream is;
    private OutputStream os;

    protected UserConfig(Socket connection) {
        try {
            this.connection = connection;

            this.is = connection.getInputStream();
            this.os = connection.getOutputStream();

            this.inputStreamReader = new InputStreamReader(is, enc);
            this.outputStreamWriter = new OutputStreamWriter(os, enc);

            this.userReader = new BufferedReader(inputStreamReader);
            this.userWriter = new BufferedWriter(outputStreamWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Charset getEnc() {
        return enc;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

    public BufferedReader getUserReader() {
        return userReader;
    }

    public void setUserReader(BufferedReader userReader) {
        this.userReader = userReader;
    }

    public BufferedWriter getUserWriter() {
        return userWriter;
    }

    public void setUserWriter(BufferedWriter userWriter) {
        this.userWriter = userWriter;
    }

    public InputStreamReader getInputStreamReader() {
        return inputStreamReader;
    }

    public void setInputStreamReader(InputStreamReader inputStreamReader) {
        this.inputStreamReader = inputStreamReader;
    }

    public OutputStreamWriter getOutputStreamWriter() {
        return outputStreamWriter;
    }

    public void setOutputStreamWriter(OutputStreamWriter outputStreamWriter) {
        this.outputStreamWriter = outputStreamWriter;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public OutputStream getOs() {
        return os;
    }

    public void setOs(OutputStream os) {
        this.os = os;
    }
}
