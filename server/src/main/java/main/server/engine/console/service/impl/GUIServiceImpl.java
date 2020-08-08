package main.server.engine.console.service.impl;

import main.server.engine.console.models.GUIMessage;
import main.server.engine.console.service.GUIService;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
import static main.server.utils.Constants.*;


//TODO FIX
public class GUIServiceImpl implements GUIService {
    private final static JFrame mainFrame = new JFrame(SERVER_NAME);
    private final static JPanel panel = new JPanel();

    private final static JTextField input = new JTextField();
    private final static JTextPane output = new JTextPane();

    private final static JScrollPane scroll =
            new JScrollPane(output, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

    public GUIServiceImpl() {
        final Color myBackgroundColor = new Color(0, 0, 0);
        final Color myForegroundColor = new Color(59, 59, 59);

        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(700, 540);

        input.setBackground(myForegroundColor);
        input.setBorder(createEmptyBorder());
        input.setForeground(Color.LIGHT_GRAY);
        input.setCaretColor(Color.CYAN);

        scroll.setBorder(createEmptyBorder());
        output.setMargin(new Insets(3, 5, 2, 5));
        output.setBackground(myBackgroundColor);
        output.setForeground(Color.LIGHT_GRAY);
        output.setEditable(false);

        panel.setLayout(new BorderLayout());
        panel.add(scroll);
        panel.add(input, BorderLayout.SOUTH);

        mainFrame.add(panel);
        mainFrame.setVisible(true);

        input.requestFocus();
    }

    public void print(GUIMessage guiMessage) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, guiMessage.getMessageColor());

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, CONSOLE_FONT_FAMILY);
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        aset = sc.addAttribute(aset, StyleConstants.FontSize, CONSOLE_FONT_SIZE);
        aset = sc.addAttribute(aset, StyleConstants.Bold, CONSOLE_FONT_IS_BOLD_FALSE);

        Document outputDocument = output.getDocument();

        int outDocumentLength = outputDocument.getLength();
        try {
            output.getStyledDocument().insertString(outDocumentLength, guiMessage.getMessage(), aset);
            output.setCaretPosition(outDocumentLength);
            input.setText(CONSOLE_EMPTY_STRING);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
    }

    public void clearOut() {
        output.setText(CONSOLE_EMPTY_STRING);
    }

    public void clearInput() {
        input.setText(CONSOLE_EMPTY_STRING);
    }

    public JTextField getInput() {
        return input;
    }
}
