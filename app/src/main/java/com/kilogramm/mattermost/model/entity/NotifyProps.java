package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class NotifyProps extends RealmObject implements Parcelable {

    @PrimaryKey
    private long id;
    @SerializedName("all")
    @Expose
    private String all;
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
    private String desktopSound;
    @SerializedName("desktop_duration")
    @Expose
    private String desktopDuration;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("mention_keys")
    @Expose
    private String mentionKeys;
    @SerializedName("push")
    @Expose
    private String push;
    @SerializedName("push_status")
    @Expose
    private String pushStatus;

    public String getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(String pushStatus) {
        this.pushStatus = pushStatus;
    }

    public String getPush() {
        return push;
    }

    public void setPush(String push) {
        this.push = push;
    }

    public String getDesktopDuration() {
        return desktopDuration;
    }

    public void setDesktopDuration(String desktopDuration) {
        this.desktopDuration = desktopDuration;
    }
    /**
     *
     * @return
     * The all
     */
    public String getAll() {
        return all;
    }

    /**
     *
     * @param all
     * The all
     */
    public void setAll(String all) {
        this.all = all;
    }

    /**
     *
     * @return
     * The channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     *
     * @param channel
     * The channel
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     *
     * @return
     * The desktop
     */
    public String getDesktop() {
        return desktop;
    }

    /**
     *
     * @param desktop
     * The desktop
     */
    public void setDesktop(String desktop) {
        this.desktop = desktop;
    }

    /**
     *
     * @return
     * The desktopSound
     */
    public String getDesktopSound() {
        return desktopSound;
    }

    /**
     *
     * @param desktopSound
     * The desktop_sound
     */
    public void setDesktopSound(String desktopSound) {
        this.desktopSound = desktopSound;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param firstName
     * The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     * The mentionKeys
     */
    public String getMentionKeys() {
        return mentionKeys;
    }

    /**
     *
     * @param mentionKeys
     * The mention_keys
     */
    public void setMentionKeys(String mentionKeys) {
        this.mentionKeys = mentionKeys;
    }

    /**
     *
     * @return
     * The comments
     */
    public String getComments() {
        return comments;
    }

    /**
     *
     * @param comments
     * The comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    public NotifyProps(NotifyProps props) {
        this.all = props.getAll();
        this.channel = props.getChannel();
        this.desktop = props.getDesktop();
        this.desktopSound = props.getDesktopSound();
        this.desktopDuration = props.getDesktopDuration();
        this.email = props.getEmail();
        this.firstName = props.getFirstName();
        this.mentionKeys = props.getMentionKeys();
        this.comments = props.getComments();
        this.push = props.getPush();
        this.pushStatus = props.getPushStatus();
    }

    public NotifyProps() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.all);
        dest.writeString(this.channel);
        dest.writeString(this.comments);
        dest.writeString(this.desktop);
        dest.writeString(this.desktopSound);
        dest.writeString(this.desktopDuration);
        dest.writeString(this.email);
        dest.writeString(this.firstName);
        dest.writeString(this.mentionKeys);
        dest.writeString(this.push);
        dest.writeString(this.pushStatus);
    }

    protected NotifyProps(Parcel in) {
        this.id = in.readLong();
        this.all = in.readString();
        this.channel = in.readString();
        this.comments = in.readString();
        this.desktop = in.readString();
        this.desktopSound = in.readString();
        this.desktopDuration = in.readString();
        this.email = in.readString();
        this.firstName = in.readString();
        this.mentionKeys = in.readString();
        this.push = in.readString();
        this.pushStatus = in.readString();
    }

    public static final Creator<NotifyProps> CREATOR = new Creator<NotifyProps>() {
        @Override
        public NotifyProps createFromParcel(Parcel source) {
            return new NotifyProps(source);
        }

        @Override
        public NotifyProps[] newArray(int size) {
            return new NotifyProps[size];
        }
    };
}