package main.models.dto;

import main.engine.console.service.GUIService;

//TODO FIX
public class LinkGui {
    private static GUIService guiService;

    public static void setGuiService(GUIService guiService) {
        LinkGui.guiService = guiService;
    }

    public static GUIService getGuiService() {
        return guiService;
    }
}
