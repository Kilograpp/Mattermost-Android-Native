package com.kilogramm.mattermost.model.entity.member;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 08.11.2016.
 */

public class Member extends RealmObject {

    @SerializedName("channel_id")
    @Expose
    private String channelId;
    @PrimaryKey
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("mention_count")
    @Expose
    private Integer mentionCount;
    @SerializedName("msg_count")
    @Expose
    private Integer msgCount;


    public Member() {
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
    public Integer getMentionCount() {
        return mentionCount;
    }
    public void setMentionCount(Integer mentionCount) {
        this.mentionCount = mentionCount;
    }
    public Integer getMsgCount() {
        return msgCount;
    }
}
