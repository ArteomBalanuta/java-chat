package main.server.models.user;

import main.server.models.message.UserMessage;
import main.server.models.user.config.UserConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Optional;

import static main.server.utils.Utils.generateTrip;
import static main.server.utils.Utils.isNotNullOrEmpty;

public class User extends UserConfig {
    public String trip;

    private PublicKey userPublicKey;
    private byte[] sharedKey;

    private boolean isEncrypted = false;

    public User(Socket connection) {
        super(connection);
        this.trip = generateTrip();
    }

    public void setIsEncrypted(boolean flag){
        isEncrypted = flag;
    }

    public boolean isEncrypted(){
        return isEncrypted;
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

    public Optional<UserMessage> getMessage() {
        BufferedReader reader = this.getUserReader();
        Optional<UserMessage> userMessage = Optional.empty();
        try {
            if (reader.ready()) {
                String trimmedBody = reader.readLine().trim();
                if (isNotNullOrEmpty(trimmedBody)) {
                    userMessage = Optional.of(new UserMessage(this.getTrip(), trimmedBody));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userMessage;
    }
}