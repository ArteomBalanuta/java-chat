package main.models.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message {
    private String owner;
    private String body;
    private String message;

    private final char space = ' ';
    private final char separator = ':';
    private final char eol = '\n';

    public Message(String owner, String body) {
        this.owner = owner;
        this.body = body;
        this.message = this.owner +
                separator +
                space +
                body +
                eol;
    }
}

