package main.engine.console.models.command;

import main.engine.console.models.GUIMessage;

import java.awt.*;

public class Help extends Command {
    private String cmd = "help";

    @Override
    public void execute() {
        GUIMessage guiMessage = new GUIMessage(cmd, Color.GRAY, true);
        guiService.print(guiMessage);

        guiMessage = new GUIMessage(" start, stop - to start,stop spreading messages across users", Color.GRAY, true);
        guiService.print(guiMessage);
    }

    @Override
    public String getCommandString() {
        return this.cmd;
    }
}
