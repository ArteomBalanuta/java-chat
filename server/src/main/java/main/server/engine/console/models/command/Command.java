package main.server.engine.console.models.command;

import main.server.engine.console.service.GUIService;
import main.server.engine.server.facade.ChatEngine;

public abstract class Command {
    protected static GUIService guiService;
    protected static ChatEngine chatEngine;

    public static void setUp(GUIService guiService, ChatEngine chatEngine) {
        Command.guiService = guiService;
        Command.chatEngine = chatEngine;
    }

    public abstract void execute();

    public abstract String getString();
}
