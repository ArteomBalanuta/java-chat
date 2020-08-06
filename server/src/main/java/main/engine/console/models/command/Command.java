package main.engine.console.models.command;

import main.engine.console.service.GUIService;
import main.engine.server.Chat;

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
