package main.server.engine.console.models.command;

import main.server.engine.console.models.GUIMessage;

import java.awt.*;

public class Start extends Command {
    private String cmd = "start";

    @Override
    public void execute() {
        GUIMessage guiMessage = new GUIMessage(cmd, Color.GRAY, true);
        guiService.print(guiMessage);
        chatEngine.start();
    }

    @Override
    public String getString() {
        return this.cmd;
    }
}


