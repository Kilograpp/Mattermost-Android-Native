package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ngers on 20.12.16.
 */

public class Broadcast implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.channel_id);
        if(this.omit_users!=null) {
            dest.writeInt(this.omit_users.size());
            for (Map.Entry<String, Boolean> entry : this.omit_users.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeValue(entry.getValue());
            }
        }
        dest.writeString(this.team_id);
        dest.writeString(this.user_id);
    }

    protected Broadcast(Parcel in) {
        this.channel_id = in.readString();
        int omit_usersSize = in.readInt();
        this.omit_users = new HashMap<>(omit_usersSize);
        for (int i = 0; i < omit_usersSize; i++) {
            String key = in.readString();
            Boolean value = (Boolean) in.readValue(Boolean.class.getClassLoader());
            this.omit_users.put(key, value);
        }
        this.team_id = in.readString();
        this.user_id = in.readString();
    }

    public static final Parcelable.Creator<Broadcast> CREATOR = new Parcelable.Creator<Broadcast>() {
        @Override
        public Broadcast createFromParcel(Parcel source) {
            return new Broadcast(source);
        }

        @Override
        public Broadcast[] newArray(int size) {
            return new Broadcast[size];
        }
    };
}
