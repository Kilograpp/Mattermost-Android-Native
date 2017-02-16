package com.kilogramm.mattermost.rxtest.left_menu.direct;

import com.kilogramm.mattermost.model.entity.user_v2.UserV2;

/**
 * Created by Evgeny on 17.01.2017.
 */

public class DirectItem extends IDirect {

    private String channelId;
    private String userId;
    private String username;
    private String status;
    private int mentionCount;
    private int totalMessageCount;
    private int msgCount;
    private boolean inTeam = false;
    private boolean isUpdate = false;


    public DirectItem() {
    }

    public DirectItem(UserV2 user){
//        this.channelId = user.get

        this.userId = user.getId();
        this.username = user.getUsername();
        this.status = user.getStatus();
        this.inTeam = Boolean.valueOf(user.getInTeam());
        // TODO: 13.02.17 initialize other fields
    }

    public String getChannelId() {
        return channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMentionCount() {
        return mentionCount;
    }

    public void setMentionCount(int mentionCount) {
        this.mentionCount = mentionCount;
    }

    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    public void setTotalMessageCount(int totalMessageCount) {
        this.totalMessageCount = totalMessageCount;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public boolean isInTeam() {
        return inTeam;
    }

    public void setInTeam(boolean inTeam) {
        this.inTeam = inTeam;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }



    @Override
    public int getType() {
        return TYPE_ITEM;
    }

    @Override
    public String toString() {
        return "DirectItem{" +
                "channelId='" + channelId + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", mentionCount=" + mentionCount +
                ", totalMessageCount=" + totalMessageCount +
                ", msgCount=" + msgCount +
                ", inTeam=" + inTeam +
                ", isUpdate=" + isUpdate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectItem that = (DirectItem) o;

        if (mentionCount != that.mentionCount) return false;
        if (totalMessageCount != that.totalMessageCount) return false;
        if (msgCount != that.msgCount) return false;
        if (inTeam != that.inTeam) return false;
        if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;
        return status != null ? status.equals(that.status) : that.status == null;

    }

    @Override
    public int hashCode() {
        int result = channelId != null ? channelId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + mentionCount;
        result = 31 * result + totalMessageCount;
        result = 31 * result + msgCount;
        result = 31 * result + (inTeam ? 1 : 0);
        return result;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
