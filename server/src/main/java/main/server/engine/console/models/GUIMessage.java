package main.server.engine.console.models;

import java.awt.*;

public class GUIMessage {
    public String message;
    public Color messageColor;
    public boolean isNewLine;

    public GUIMessage(String message) {
        this.isNewLine = true;
        this.message = message + '\n';
        this.messageColor = Color.CYAN;
    }

    public GUIMessage(String message, Color color, boolean isNewLine) {
        this.isNewLine = isNewLine;
        this.message = isNewLine ? message + '\n' : message;
        this.messageColor = color;
    }

    public Color getMessageColor(){
        return this.messageColor;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setMessageColor(Color color){
        this.messageColor = color;
    }

}
