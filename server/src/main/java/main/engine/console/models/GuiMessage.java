package main.engine.console.models;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;

@Getter
@Setter
public class GuiMessage {
    String message;
    Color messageColor;
    boolean isNewLine;

    public GuiMessage(String message) {
        this.isNewLine = true;
        this.message = message + '\n';
        this.messageColor = Color.CYAN;
    }

    public GuiMessage(String message, Color color, boolean isNewLine) {
        this.isNewLine = isNewLine;
        this.message = isNewLine ? message + '\n' : message;
        this.messageColor = color;
    }

}