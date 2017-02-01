package com.kilogramm.mattermost.rxtest.left_menu.direct;

/**
 * Created by Evgeny on 17.01.2017.
 */

public class DirectItem extends IDirect {

    public String channelId;
    public String userId;
    public String username;
    public String status;
    public int mentionCount;
    public int totalMessageCount;
    public int msgCount;
    public boolean inTeam = false;
    public boolean isUpdate = false;

    public DirectItem() {
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
}
