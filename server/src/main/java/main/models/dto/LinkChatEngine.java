package main.models.dto;


import main.engine.server.Chat;
//TODO FIX
public class LinkChatEngine {
    private static Chat chatEngine;

    public static void setChat(Chat chat) {
        chatEngine = chat;
    }

    public static Chat getChat() {
        return chatEngine;
    }
}
