package main.engine.console.models.command;

import main.engine.console.gui.Gui;
import main.engine.server.Chat;
import main.models.dto.LinkChatEngine;
import main.models.dto.LinkGui;

public abstract class Command {
    protected static Gui gui = LinkGui.getGui();
    protected static Chat chat = LinkChatEngine.getChat();

    public abstract void execute();
    public abstract String getCommandString();
}
