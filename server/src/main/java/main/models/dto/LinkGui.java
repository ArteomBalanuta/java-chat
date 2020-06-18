package main.models.dto;

import main.engine.console.gui.Gui;

public class LinkGui {
    private static Gui gui;

    public static void setGui(Gui gui) {
        LinkGui.gui = gui;
    }

    public static Gui getGui() {
        return gui;
    }
}
