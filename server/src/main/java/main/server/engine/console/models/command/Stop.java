package main.server.engine.console.models.command;

import main.server.engine.console.models.GUIMessage;

import java.awt.*;

public class Stop extends Command {
    private String cmd = "stop";

    @Override
    public void execute() {
        GUIMessage guiMessage = new GUIMessage(cmd, Color.GRAY, true);
        guiService.print(guiMessage);
        chat.stop();
    }

    @Override
    public String getCommandString() {
        return this.cmd;
    }
}

