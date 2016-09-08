package com.kilogramm.mattermost.model.websocket;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class WebSocketObj {

    public static final String TEAM_ID = "team_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String USER_ID = "user_id";
    public static final String ACTION = "action";
    public static final String PROPS = "props";

    public static final String ACTION_POSTED = "posted";
    public static final String ACTION_CHANNEL_VIEWED = "channel_viewed";
    public static final String ACTION_TYPING = "typing";

    @SerializedName("team_id")
    @Expose
    private String teamId;
    @SerializedName("channel_id")
    @Expose
    private String channelId;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("props")
    @Expose
    private String props;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }
}
