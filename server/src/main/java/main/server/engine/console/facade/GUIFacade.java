package main.server.engine.console.facade;

import main.server.engine.console.service.CMDService;
import main.server.engine.console.service.GUIMessageService;
import main.server.engine.console.service.GUIService;

public interface GUIFacade {
    void startConsole();
    GUIService getGuiService();
    GUIMessageService getGuiMessageService();
}
