package main.engine.console.service;

import main.engine.console.models.GUIMessage;

import javax.swing.*;

//TODO FIX
public interface GUIService {
    void print(GUIMessage guiMessage);
    void clearOut();
    void clearInput();

    JTextField getInput();
}
