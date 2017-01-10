package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;

import java.util.List;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class ChannelsWithMembers {

    @SerializedName("channels")
    @Expose
    private List<Channel> channels;

    @SerializedName("members")
    @Expose
    private List<Member> members;

    public ChannelsWithMembers(List<Channel> channels, List<Member> members) {
        this.channels = channels;
        this.members = members;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }
}
