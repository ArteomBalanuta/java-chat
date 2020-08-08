package main.client.service;

import java.nio.charset.Charset;

public interface ConnectionService {
    String read();

    void write(String msg);

    void setConnection(String host, int port);

    void setEnc(Charset enc);

}
