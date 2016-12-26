package com.kilogramm.mattermost.model.entity.channel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by melkshake on 19.12.16.
 */

public class Stats {

    @SerializedName("channel_id")
    @Expose
    private String channelId;
    @SerializedName("member_count")
    @Expose
    private String memberCount;

    public String getChannelId() {
        return channelId;
    }

    public String getMemberCount() {
        return memberCount;
    }
}
