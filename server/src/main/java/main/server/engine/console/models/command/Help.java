package main.server.engine.console.models.command;

import main.server.engine.console.models.GUIMessage;

import java.awt.*;

public class Help extends Command {
    private String cmd = "help";

    @Override
    public void execute() {
        GUIMessage guiMessage = new GUIMessage(cmd, Color.GRAY, true);
        guiService.print(guiMessage);

        guiMessage = new GUIMessage(" start, stop - starts, stops sharing messages across users", Color.GRAY, true);
        guiService.print(guiMessage);
    }

    @Override
    public String getString() {
        return this.cmd;
    }
}
