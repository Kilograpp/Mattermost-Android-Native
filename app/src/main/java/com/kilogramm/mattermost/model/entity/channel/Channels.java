package com.kilogramm.mattermost.model.entity.channel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by melkshake on 19.12.16.
 */

public class Channels {
    @SerializedName("channels")
    List<Channel> channels;

    public Channels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "Channels{" +
                "channels=" + channels +
                '}';
    }
}
