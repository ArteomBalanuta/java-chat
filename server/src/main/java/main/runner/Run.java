package main.runner;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Run {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(800);

            String str;
            while (true) {
                Thread.sleep(100);
                Socket s = server.accept();

                DataInputStream dis = new DataInputStream(s.getInputStream());

                str = dis.readUTF();
                System.out.println("Message= "+ str);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
