package main.engine.console.gui;

import main.engine.console.models.Message;

import javax.swing.*;

public interface Gui {
    void print(Message message);
    void clearOut();
    void clearInput();

    JTextField getInput();
}
