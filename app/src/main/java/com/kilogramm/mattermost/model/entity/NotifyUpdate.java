package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jeniks on 25.07.2016.
 */
public class NotifyUpdate implements Parcelable {

    private String user_id;
    private String all;
    private String channel;
    private String comments;
    private String desktop;
    private String desktop_sound;
    private String desktop_duration;
    private String email;
    private String first_name;
    private String mention_keys;
    private String push;
    private String push_status;


    public NotifyUpdate(NotifyProps props, String user_id) {
        this.all = props.getAll();
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
        dest.writeString(this.all);
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
        this.all = in.readString();
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