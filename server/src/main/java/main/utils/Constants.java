package main.utils;

import main.engine.console.models.GuiMessage;

public class Constants {
    public static final String SERVER_NAME = "JServer";
    public static final String SERVER_STARTED = "Server running...";
    public static final String SERVER_ONLINE = "Server online %s: %s";

    public static final String MESSAGE_USER_LEFT = "%s left";
    public static final String MESSAGE_USER_JOIN = "%s joined";

    public static final String CONSOLE_FONT_FAMILY = "Consolas";
    public static final String CONSOLE_EMPTY_STRING = "";
    public static final int CONSOLE_FONT_SIZE = 12;

    public static final boolean CONSOLE_FONT_IS_BOLD_TRUE = true;
    public static final boolean CONSOLE_FONT_IS_BOLD_FALSE = false;

    GuiMessage GUI_MESSAGE_INVALID_CMD = new GuiMessage("Invalid command!");

    public static final String CONSOlE_CMD_HELP = "help";
    public static final String CONSOLE_CMD_START = "start";
    public static final String CONSOLE_CMD_STOP = "stop";

}
