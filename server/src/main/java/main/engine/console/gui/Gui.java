package main.engine.console.gui;

import main.engine.console.models.GuiMessage;

import javax.swing.*;

public interface Gui {
    void print(GuiMessage guiMessage);
    void clearOut();
    void clearInput();

    JTextField getInput();
}
