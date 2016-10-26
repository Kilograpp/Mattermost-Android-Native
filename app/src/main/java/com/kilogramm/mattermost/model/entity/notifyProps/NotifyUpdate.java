package com.kilogramm.mattermost.model.entity.notifyProps;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Jeniks on 25.07.2016.
 */
public class NotifyUpdate implements Parcelable {
    @SerializedName("user_id")
    @Expose
    private String user_id;
    @SerializedName("channel")
    @Expose
    private String channel;
    @SerializedName("comments")
    @Expose
    private String comments;
    @SerializedName("desktop")
    @Expose
    private String desktop;
    @SerializedName("desktop_sound")
    @Expose
    private String desktop_sound;
    @SerializedName("desktop_duration")
    @Expose
    private String desktop_duration;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("first_name")
    @Expose
    private String first_name;
    @SerializedName("mention_keys")
    @Expose
    private String mention_keys;
    @SerializedName("push")
    @Expose
    private String push;
    @SerializedName("push_status")
    @Expose
    private String push_status;


    public NotifyUpdate(NotifyProps props, String user_id) {
        this.channel = props.getChannel();
        this.desktop = props.getDesktop();
        this.desktop_sound = props.getDesktopSound();
        this.desktop_duration = props.getDesktopDuration();
        this.email = props.getEmail();
        this.first_name = props.getFirstName();
        this.mention_keys = props.getMentionKeys();
        this.push = props.getPush();
        this.push_status = props.getPushStatus();
        this.comments = props.getComments();
        this.user_id = user_id;
    }

    public NotifyUpdate() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.user_id);
        dest.writeString(this.channel);
        dest.writeString(this.comments);
        dest.writeString(this.desktop);
        dest.writeString(this.desktop_sound);
        dest.writeString(this.desktop_duration);
        dest.writeString(this.email);
        dest.writeString(this.first_name);
        dest.writeString(this.mention_keys);
        dest.writeString(this.push);
        dest.writeString(this.push_status);
    }

    protected NotifyUpdate(Parcel in) {
        this.user_id = in.readString();
        this.channel = in.readString();
        this.comments = in.readString();
        this.desktop = in.readString();
        this.desktop_sound = in.readString();
        this.desktop_duration = in.readString();
        this.email = in.readString();
        this.first_name = in.readString();
        this.mention_keys = in.readString();
        this.push = in.readString();
        this.push_status = in.readString();
    }

    public static final Creator<NotifyUpdate> CREATOR = new Creator<NotifyUpdate>() {
        @Override
        public NotifyUpdate createFromParcel(Parcel source) {
            return new NotifyUpdate(source);
        }

        @Override
        public NotifyUpdate[] newArray(int size) {
            return new NotifyUpdate[size];
        }
    };
}