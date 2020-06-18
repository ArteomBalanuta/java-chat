package main.engine.console.models.command;

import java.awt.*;

import main.engine.console.models.GuiMessage;

public class Help extends Command {
    private String cmd = "help";

    @Override
    public void execute() {
        GuiMessage guiMessage = new GuiMessage(cmd, Color.GRAY, true);
        gui.print(guiMessage);
    }

    @Override
    public String getCommandString() {
        return this.cmd;
    }
}
