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
                '}';
    }
}
