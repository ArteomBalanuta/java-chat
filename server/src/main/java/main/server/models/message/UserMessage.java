package main.server.models.message;

//TODO FIX
public class UserMessage {
    private final char space = ' ';
    private final char separator = ':';

    private String owner;
    private String body;
    private String message;

    private boolean isEncrypted;

    public UserMessage() {
    }

    public UserMessage(String owner, String body) {
        this.owner = owner;
        this.body = body;
        this.message = this.owner +
                separator +
                space +
                body;
    }

    public boolean isKeyExchangeMessage(){
        return this.body.contains("publicKey ");
    }

    public void setIsEncrypted(boolean flag){
        isEncrypted = flag;
    }

    public boolean isEncrypted(){
        return isEncrypted;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

