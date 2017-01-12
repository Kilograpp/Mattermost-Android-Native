package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 11.01.2017.
 */

public class AutocompleteUsers {

    @SerializedName("in_channel")
    private List<User> inChannel = new ArrayList<>();
    @SerializedName("out_of_channel")
    private List<User> outChannel = new ArrayList<>();

    public AutocompleteUsers(List<User> inChannel, List<User> outChannel) {
        this.inChannel = inChannel;
        this.outChannel = outChannel;
    }

    public List<User> getInChannel() {
        return inChannel;
    }

    public void setInChannel(List<User> inChannel) {
        this.inChannel = inChannel;
    }

    public List<User> getOutChannel() {
        return outChannel;
    }

    public void setOutChannel(List<User> outChannel) {
        this.outChannel = outChannel;
    }
}
