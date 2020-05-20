package com.projectsalvation.pigeotalk.DAO;

public class GroupChatDAO {

    private String chatId;
    private String photoUrl;
    private String groupName;
    private String lastMessage;
    private String messageType;
    private String timestamp;
    private String unreadMessageCount;
    private String isMuted;
    private String senderId;

    public GroupChatDAO(String chatId,
                        String photoUrl,
                        String groupName,
                        String lastMessage,
                        String messageType, String timestamp,
                        String unreadMessageCount,
                        String isMuted,
                        String senderId) {
        this.chatId = chatId;
        this.photoUrl = photoUrl;
        this.groupName = groupName;
        this.lastMessage = lastMessage;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.unreadMessageCount = unreadMessageCount;
        this.isMuted = isMuted;
        this.senderId = senderId;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
