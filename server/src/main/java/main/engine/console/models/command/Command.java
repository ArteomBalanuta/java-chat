package main.engine.console.models.command;

import main.engine.console.gui.Gui;
import main.engine.console.gui.GuiImpl;

public abstract class Command {
    static Gui gui = new GuiImpl();

    public abstract void execute();
    public abstract String getCommandString();
}
