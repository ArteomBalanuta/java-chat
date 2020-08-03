package main.engine.console.models.command;

import main.engine.console.service.GUIService;
import main.engine.server.Chat;
import main.models.dto.LinkChatEngine;
import main.models.dto.LinkGui;

public abstract class Command {
    static GUIService guiService = LinkGui.getGuiService();
    static Chat chat = LinkChatEngine.getChat();

    public abstract void execute();
    public abstract String getCommandString();
}
