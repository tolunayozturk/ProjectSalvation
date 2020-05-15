package com.projectsalvation.pigeotalk.DAO;

import android.util.Log;

import androidx.annotation.NonNull;

public class MessageDAO {

    private String message;
    private String messageType;
    private String timestamp;
    private String recipient;
    private String sender;
    private String messageId;
    private String isRead;
    private String seenAt;
    private String chatId;

    public MessageDAO(String message, String messageType,
                      String timestamp, String recipient,
                      String sender, String messageId,
                      String isRead, String seenAt, String chatId) {
        this.message = message;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.recipient = recipient;
        this.sender = sender;
        this.messageId = messageId;
        this.isRead = isRead;
        this.seenAt = seenAt;
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(String seenAt) {
        this.seenAt = seenAt;
    }
}
