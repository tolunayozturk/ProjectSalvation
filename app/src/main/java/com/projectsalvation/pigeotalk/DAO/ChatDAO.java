package com.projectsalvation.pigeotalk.DAO;

public class ChatDAO {

    private String chatId;
    private String photoUrl;
    private String name;
    private String lastMessage;
    private String timestamp;
    private String unreadMessageCount;
    private String isMuted;
    private String userId;

    public ChatDAO(String chatId,
                   String photoUrl,
                   String name,
                   String lastMessage,
                   String timestamp,
                   String unreadMessageCount,
                   String isMuted, String userId) {
        this.chatId = chatId;
        this.photoUrl = photoUrl;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadMessageCount = unreadMessageCount;
        this.isMuted = isMuted;
        this.userId = userId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessageId(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(String unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public String getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(String isMuted) {
        this.isMuted = isMuted;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
