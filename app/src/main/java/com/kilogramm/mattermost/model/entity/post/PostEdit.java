package com.kilogramm.mattermost.model.entity.post;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Jeniks on 18.08.2016.
 */
public class PostEdit implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("channel_id")
    @Expose
    private String channelId;
    @SerializedName("message")
    @Expose
    private String message;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public PostEdit(Post post) {
        this.id = post.getId();
        this.message = post.getMessage();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.message);
    }

    public PostEdit() {
    }

    protected PostEdit(Parcel in) {
        this.id = in.readString();
        this.message = in.readString();
    }

    public static final Parcelable.Creator<PostEdit> CREATOR = new Parcelable.Creator<PostEdit>() {
        @Override
        public PostEdit createFromParcel(Parcel source) {
            return new PostEdit(source);
        }

        @Override
        public PostEdit[] newArray(int size) {
            return new PostEdit[size];
        }
    };
}