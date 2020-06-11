package main.models.user;

import main.models.user.config.UserConfig;

import java.net.Socket;

public class User extends UserConfig {

    public User(Socket connection) {
        super(connection);
    }

}
