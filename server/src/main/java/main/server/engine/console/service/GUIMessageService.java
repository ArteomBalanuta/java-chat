package main.server.engine.console.service;

import main.server.engine.console.models.GUIMessage;

import java.util.Optional;

public interface GUIMessageService {

    void addMessage(GUIMessage guiMessage);

    Optional<GUIMessage> getMessage();
}
