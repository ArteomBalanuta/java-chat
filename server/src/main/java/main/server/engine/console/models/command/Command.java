package main.server.engine.console.models.command;

import main.server.engine.console.service.GUIService;
import main.server.engine.server.facade.Chat;

public abstract class Command {
    protected static GUIService guiService;
    protected static Chat chat;

    public static void setUp(GUIService guiService, Chat chat) {
        Command.guiService = guiService;
        Command.chat = chat;
    }

    public abstract void execute();

    public abstract String getCommandString();
}
