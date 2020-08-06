package main.engine.console.facade;

import main.engine.console.service.GUIMessageService;
import main.engine.console.service.GUIService;

public interface GUIFacade {
    void startConsole();
    GUIService getGuiService();
    GUIMessageService getGuiMessageService();
}
