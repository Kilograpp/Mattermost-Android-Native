package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.user.User;

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
    private Map<String,User> members;


    public Map<String, User> getMembers() {
        return members;
    }

    public void setMembers(Map<String, User> members) {
        this.members = members;
    }

    public RealmList<Channel> getChannels() {
        return channels;
    }

    public void setChannels(RealmList<Channel> channels) {
        this.channels = channels;
    }
}
