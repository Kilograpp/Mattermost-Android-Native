package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by ngers on 20.12.16.
 */

public class Broadcast {

    @SerializedName("channel_id")
    @Expose
    private String channel_id;
    @SerializedName("omit_users")
    @Expose
    private Map<String, Boolean> omit_users;
    @SerializedName("team_id")
    @Expose
    private String team_id;
    @SerializedName("user_id")
    @Expose
    private String user_id;

    public Broadcast(String channel_id, Map<String, Boolean> omit_users, String team_id, String user_id) {
        this.channel_id = channel_id;
        this.omit_users = omit_users;
        this.team_id = team_id;
        this.user_id = user_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public Map<String, Boolean> getOmit_users() {
        return omit_users;
    }

    public void setOmit_users(Map<String, Boolean> omit_users) {
        this.omit_users = omit_users;
    }

    public String getTeam_id() {
        return team_id;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
