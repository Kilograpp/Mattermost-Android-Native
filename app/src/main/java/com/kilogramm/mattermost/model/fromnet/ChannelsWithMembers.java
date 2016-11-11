package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.channel.Channel;

import java.util.Map;

import io.realm.RealmList;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class ChannelsWithMembers {

    @SerializedName("channels")
    @Expose
    private RealmList<Channel> channels;

    @SerializedName("members")
    @Expose
    private Map<String,Member> members;


    public Map<String, Member> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }

    public RealmList<Channel> getChannels() {
        return channels;
    }

    public void setChannels(RealmList<Channel> channels) {
        this.channels = channels;
    }
}
