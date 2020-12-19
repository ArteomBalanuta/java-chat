package main.server.engine.console.models.command;

import main.server.engine.console.models.GUIMessage;

import java.awt.*;

public class Stop extends Command {
    private String cmd = "stop";

    @Override
    public void execute() {
        GUIMessage guiMessage = new GUIMessage(cmd, Color.GRAY, true);
        guiService.print(guiMessage);
        chatEngine.stop();
    }

    @Override
    public String getString() {
        return this.cmd;
    }
}

