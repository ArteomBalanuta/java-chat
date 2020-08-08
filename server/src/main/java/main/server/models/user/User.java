package main.server.models.user;

import main.server.models.user.config.UserConfig;

import java.net.Socket;
import java.security.PublicKey;

import static main.server.utils.Utils.generateTrip;

public class User extends UserConfig {
    public String trip;

    private PublicKey userPublicKey;
    private byte[] sharedKey;

    public User(Socket connection) {
        super(connection);
        this.trip = generateTrip();
    }

    public PublicKey getUserPublicKey() {
        return userPublicKey;
    }

    public void setUserPublicKey(PublicKey userPublicKey) {
        this.userPublicKey = userPublicKey;
    }

    public byte[] getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(byte[] sharedKey) {
        this.sharedKey = sharedKey;
    }

    public String getTrip() {
        return trip;
    }

    public void setTrip(String trip) {
        this.trip = trip;
    }
}
