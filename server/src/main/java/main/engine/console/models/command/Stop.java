package main.engine.console.models.command;

import java.awt.*;

import main.engine.console.models.GuiMessage;

public class Stop extends Command {
    private String cmd = "stop";

    @Override
    public void execute() {
        GuiMessage guiMessage = new GuiMessage(cmd, Color.GRAY, true);
        gui.print(guiMessage);
        chat.stop();
    }

    @Override
    public String getCommandString() {
        return this.cmd;
    }
}

