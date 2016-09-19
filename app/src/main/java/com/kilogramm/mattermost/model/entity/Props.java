package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class Props {
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
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("team_id")
    @Expose
    private String teamId;

    public Props(String channelDisplayName,
                 String channelType,
                 String mentions,
                 Post post,
                 String senderName,
                 String teamId) {
        this.channelDisplayName = channelDisplayName;
        this.channelType = channelType;
        this.mentions = mentions;
        this.post = post;
        this.senderName = senderName;
        this.teamId = teamId;
    }

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getMentions() {
        return mentions;
    }

    public Post getPost() {
        return post;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTeamId() {
        return teamId;
    }
}
