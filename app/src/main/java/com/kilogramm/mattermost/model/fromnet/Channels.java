package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.Channel;

import io.realm.RealmList;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class Channels {

    @SerializedName("channels")
    @Expose
    private RealmList<Channel> channels;

    public RealmList<Channel> getChannels() {
        return channels;
    }

    public void setChannels(RealmList<Channel> channels) {
        this.channels = channels;
    }
}
