package main.models.user.config;


import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static main.utils.Utils.generateTrip;

@Setter
@Getter
public class UserConfig {
    private final Charset enc = ISO_8859_1;

    public String trip;
    private Socket connection;
    private String publicKey;

    private BufferedReader userReader;
    private BufferedWriter userWriter;

    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    private InputStream is;
    private OutputStream os;

    protected UserConfig(Socket connection) {
        try {
            this.trip = generateTrip();
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
}
