package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 12.12.2016.
 */

public class CommandToNet {
    @SerializedName("channelId")
    private String channelId;
    @SerializedName("command")
    private String command;
    @SerializedName("suggest")
    private String suggest;

    public CommandToNet(String channelId, String command, String suggest) {
        this.channelId = channelId;
        this.command = command;
        this.suggest = suggest;
    }
}
