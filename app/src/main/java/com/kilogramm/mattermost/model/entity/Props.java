package com.kilogramm.mattermost.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.post.Post;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class Props implements Parcelable {
    @SerializedName("channel_display_name")
    @Expose
    private String channelDisplayName;
    @SerializedName("channel_type")
    @Expose
    private String channelType;
    @SerializedName("mentions")
    @Expose
    private String mentions;
    @SerializedName("post")
    @Expose
    private Post post;
    @SerializedName("sender_name")
    @Expose
    private String senderName;
    @SerializedName("parent_id")
    @Expose
    private String parentId;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("team_id")
    @Expose
    private String teamId;

    public Props(String channelDisplayName,
                 String channelType,
                 String mentions,
                 Post post,
                 String senderName,
                 String teamId) {
        this.channelDisplayName = channelDisplayName;
        this.channelType = channelType;
        this.mentions = mentions;
        this.post = post;
        this.senderName = senderName;
        this.teamId = teamId;
    }

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getMentions() {
        return mentions;
    }

    public Post getPost() {
        return post;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTeamId() {
        return teamId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.channelDisplayName);
        dest.writeString(this.channelType);
        dest.writeString(this.mentions);
        dest.writeParcelable(this.post, flags);
        dest.writeString(this.senderName);
        dest.writeString(this.parentId);
        dest.writeString(this.state);
        dest.writeString(this.teamId);
    }

    protected Props(Parcel in) {
        this.channelDisplayName = in.readString();
        this.channelType = in.readString();
        this.mentions = in.readString();
        this.post = in.readParcelable(Post.class.getClassLoader());
        this.senderName = in.readString();
        this.parentId = in.readString();
        this.state = in.readString();
        this.teamId = in.readString();
    }

    public static final Parcelable.Creator<Props> CREATOR = new Parcelable.Creator<Props>() {
        @Override
        public Props createFromParcel(Parcel source) {
            return new Props(source);
        }

        @Override
        public Props[] newArray(int size) {
            return new Props[size];
        }
    };
}
