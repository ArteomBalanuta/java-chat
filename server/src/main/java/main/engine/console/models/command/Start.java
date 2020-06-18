package main.engine.console.models.command;

import java.awt.*;

import main.engine.console.models.GuiMessage;

public class Start extends Command {
    private String cmd = "start";

    @Override
    public void execute() {
        GuiMessage guiMessage = new GuiMessage(cmd, Color.GRAY, true);
        gui.print(guiMessage);
        chat.start();
    }

    @Override
    public String getCommandString() {
        return this.cmd;
    }
}


