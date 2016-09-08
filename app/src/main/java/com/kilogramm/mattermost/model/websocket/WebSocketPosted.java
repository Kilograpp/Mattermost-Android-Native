package com.kilogramm.mattermost.model.websocket;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.Post;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class WebSocketPosted {

    public static final String CHANNEL_DISPLAY_NAME = "channel_display_name";
    public static final String CHANNEL_TYPE = "channel_type";
    public static final String CHANNEL_POST = "post";
    public static final String SENDER_NAME = "sender_name";
    public static final String TEAM_ID = "team_id";
    public static final String MENTIONS = "mentions";

    @SerializedName("channel_display_name")
    @Expose
    private String channelDisplayName;
    @SerializedName("channel_type")
    @Expose
    private String channelType;
    @SerializedName("mentions")
    @Expose
    private String mentions;
    @SerializedName("post")
    @Expose
    private Post post;
    @SerializedName("sender_name")
    @Expose
    private String senderName;
    @SerializedName("team_id")
    @Expose
    private String teamId;

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public void setChannelDisplayName(String channelDisplayName) {
        this.channelDisplayName = channelDisplayName;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getMentions() {
        return mentions;
    }

    public void setMentions(String mentions) {
        this.mentions = mentions;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
