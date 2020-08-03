package main.engine.console.models.command;

import java.awt.*;

import main.engine.console.models.GUIMessage;

public class Start extends Command {
    private String cmd = "start";

    @Override
    public void execute() {
        GUIMessage guiMessage = new GUIMessage(cmd, Color.GRAY, true);
        guiService.print(guiMessage);
        chat.start();
    }

    @Override
    public String getCommandString() {
        return this.cmd;
    }
}


